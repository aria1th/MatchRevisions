package aria1th.main.matchrevisions.mixins;

import aria1th.main.matchrevisions.utils.MessageHolder;
import aria1th.main.matchrevisions.utils.VariableHolder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;

import static aria1th.main.matchrevisions.utils.VariableHolder.allow;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow private CommandDispatcher<CommandSource> commandDispatcher;
	private static boolean isSynced = false;
	private static long lastSynced = 0L;

	private static void register(CommandDispatcher<ServerCommandSource> dispatcher){
		LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("matchrevision");
		commandBuilder.executes(context -> {
				PlayerEntity player = MinecraftClient.getInstance().player;
				player.sendMessage(Text.of("[matchrevision] current state : "+ allow));
				return 1;
			}
		);
		commandBuilder.then(literal("toggle").executes( context -> {
			allow = !allow;
			PlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null){player.sendMessage(Text.of("[matchrevision] Togged state : " + allow));} return 1;
		}));
		commandBuilder.then(literal("debug").executes(context -> {
			MessageHolder.allow = !MessageHolder.allow;
			PlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null){player.sendMessage(Text.of("[matchrevision] Debug status : " + MessageHolder.allow));}
			return 1;
		}));
		commandBuilder.then(literal("debug").then(literal("actionBar").executes(context -> {
			MessageHolder.useActionbar = !MessageHolder.useActionbar;
			PlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null){player.sendMessage(Text.of("[matchrevision] Debug actionbar status : " + MessageHolder.useActionbar));}
			return 1;
		})));
		commandBuilder.then(literal("toggleInteractBlockFeature").executes(context -> {
			VariableHolder.interactBlock = !VariableHolder.interactBlock;
			PlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null){player.sendMessage(Text.of("[matchrevision] interactBlockPatch status : " + VariableHolder.interactBlock));}
			return 1;
		}));
		dispatcher.register(commandBuilder);
	}
	/*
		SyncHandler defined at ServerPlayerEntity is responsible to sync, but actually client process actions and executes too. so Server sync does not match with client, which causes desync.
		We can see ghost items in this context, especially with high ping. But, if there's no packet loss, whatever client has executed will be done in order correctly, like click recipe -> press Q in result slot even if its empty.
		So server sync is not totally required for most cases.
	*/
	@Inject(method = "onCommandTree", at = @At("TAIL"))
	private void onJoinWorld(CommandTreeS2CPacket packet, CallbackInfo ci){
		register((CommandDispatcher<ServerCommandSource>) (Object) this.commandDispatcher);
	}
	@Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"), cancellable = true)
	private void onUpdateSlots(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
		if (!allow){
			return;
		}
		syncIfThreshold();
		final ClientPlayerEntity player = this.client.player;
		final MessageHolder messageHolder = new MessageHolder(player);
		if (!isSynced && player != null){
			isSynced = true;
			while (packet.getRevision() != player.currentScreenHandler.getRevision()) {
				player.currentScreenHandler.nextRevision();
			}
			messageHolder.sendMessage("Matched rev on start : "+ packet.getRevision());
			messageHolder.sendMessage(Text.of("Slot was "+ packet.getSlot()+ " stack was " +packet.getItemStack()));
			this.client.execute(()-> {
				if (this.client.currentScreen instanceof CreativeInventoryScreen) {
					player.playerScreenHandler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getItemStack());
				} else {
					player.currentScreenHandler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getItemStack());
				}
			});
			ci.cancel();
			return;
		}
		if (player != null){
			int rev = player.currentScreenHandler.getRevision();
			if (!isSyncScreen(this.client.currentScreen) && shouldCancel(rev, packet.getRevision())) {
				messageHolder.sendMessage(Text.of("Canceled rev : "+ packet.getRevision() + " current : "+ rev));
				messageHolder.sendMessage(Text.of("Slot was "+ packet.getSlot()+ " stack was " +packet.getItemStack()));
				ci.cancel();
			} else {
				while (packet.getRevision() != player.currentScreenHandler.getRevision()) {
					player.currentScreenHandler.nextRevision();
				}
				messageHolder.sendMessage(Text.of("Matched rev : "+ packet.getRevision() + " current : "+ rev));
				messageHolder.sendMessage(Text.of("Slot was "+ packet.getSlot()+ " stack was " +packet.getItemStack()));
				if (packet.getSlot() == -1){
					if (packet.getSyncId() == -1 && !(this.client.currentScreen instanceof CreativeInventoryScreen)) {
						this.client.execute(()->player.currentScreenHandler.setCursorStack(packet.getItemStack()));
					}
					else {
						player.sendMessage(Text.of("Slot was "+ packet.getSlot()+ " stack was " +packet.getItemStack() + " syncId was "+ packet.getSyncId()));
					}
					return;
				}
				this.client.execute(()-> {
					if (this.client.currentScreen instanceof CreativeInventoryScreen) {
						player.playerScreenHandler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getItemStack());
					} else {
						player.currentScreenHandler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getItemStack());
					}
				});
			}
		}
	}
	@Inject(method = "onDisconnect", at = @At("HEAD"))
	private void handleDisconnect(DisconnectS2CPacket packet, CallbackInfo ci){
		isSynced = false;
	}
	@Inject(method = "onCloseScreen", at = @At("HEAD"))
	private void handleDisconnect(CloseScreenS2CPacket packet, CallbackInfo ci){
		Screen screen = this.client.currentScreen;
		if (screen instanceof InventoryScreen){
			return;
		}
		isSynced = false;
	}
	private static boolean isSyncScreen(Screen screen){
		return screen instanceof MerchantScreen;
	}

	private static boolean shouldCancel(int current, int packet) {
		if (current == packet){
			return true;
		}
		int abs = Math.abs(current - packet);
		if (abs > 1024 && abs < 32760) return false;
		return Math.abs(current - packet) > 32760 ? current <= packet : current >= packet;
	}

	private static void syncIfThreshold(){
		long time = new Date().getTime();
		if (lastSynced + 4000L < time){
			isSynced = false;
			lastSynced = time;
		}
	}
}