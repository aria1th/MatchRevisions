package aria1th.main.matchrevisions.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	private static boolean isSynced = false;
	/*
		SyncHandler defined at ServerPlayerEntity is responsible to sync, but actually client process actions and executes too. so Server sync does not match with client, which causes desync.
		We can see ghost items in this context, especially with high ping. But, if there's no packet loss, whatever client has executed will be done in order correctly, like click recipe -> press Q in result slot even if its empty.
		So server sync is not totally required for most cases.
	*/
	@Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"), cancellable = true)
	private void onUpdateSlots(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
		final ClientPlayerEntity player = this.client.player;
		if (!isSynced && player != null){
			isSynced = true;
			while (packet.getRevision() != player.currentScreenHandler.getRevision()) {
				player.currentScreenHandler.nextRevision();
			}
			//player.sendMessage(Text.of("Matched rev on start : "+ packet.getRevision()));
			return;
		}
		if (player != null){
			int rev = player.currentScreenHandler.getRevision();
			if (shouldCancel(rev, packet.getRevision())) {
				//player.sendMessage(Text.of("Canceled rev : "+ packet.getRevision() + " current : "+ rev));
				//player.sendMessage(Text.of("Slot was "+ packet.getSlot()+ " stack was " +packet.getItemStack()));
				ci.cancel();
			} else {
				while (packet.getRevision() != player.currentScreenHandler.getRevision()) {
					player.currentScreenHandler.nextRevision();
				}
				//player.sendMessage(Text.of("Matched rev : "+ packet.getRevision() + " current : "+ rev));
				//player.sendMessage(Text.of("Slot was "+ packet.getSlot()+ " stack was " +packet.getItemStack()));
				player.currentScreenHandler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getItemStack());
			}
		}
	}
	@Inject(method = "onDisconnect", at = @At("HEAD"))
	private void handleDisconnect(DisconnectS2CPacket packet, CallbackInfo ci){
		isSynced = false;
	}
	

	private static boolean shouldCancel(int current, int packet) {
		if (current == packet){
			return false;
		}
		int abs = Math.abs(current - packet);
		if (abs > 1024 && abs < 32760) return false;
		return Math.abs(current - packet) > 32760 ? current < packet : current > packet;
	}
}