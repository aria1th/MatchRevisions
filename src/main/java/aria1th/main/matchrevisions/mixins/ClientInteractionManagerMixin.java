package aria1th.main.matchrevisions.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public class ClientInteractionManagerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	private static int countedValue = -1;
	@Inject(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"), require = 1)
	private void getNextRevision(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		player.currentScreenHandler.nextRevision();
	}

	@Inject(method = "clickCreativeStack", at = @At("TAIL"), require = 1)
	private void getNextRevision(ItemStack stack, int slotId, CallbackInfo ci) {
		final ClientPlayerEntity player = this.client.player;
		if (player != null) {
			if (!player.getInventory().getStack(slotId).equals(stack)){
				player.currentScreenHandler.nextRevision();
				//player.sendMessage(Text.of("Slot was "+ slotId+ " stack was " +stack));
				player.playerScreenHandler.setStackInSlot(slotId, player.playerScreenHandler.getRevision(), stack);
			}
		}
	}
	@Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"))
	private void onGenerateMutableObject(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir){
		countedValue = player.getStackInHand(hand).getCount();
	}

	@Inject(method = "interactBlockInternal", at = @At(value = "RETURN"))
	private void onReturnActionResult(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir){
		if (player.getStackInHand(hand).getCount() != countedValue){
			player.currentScreenHandler.nextRevision();
		}
	}
}