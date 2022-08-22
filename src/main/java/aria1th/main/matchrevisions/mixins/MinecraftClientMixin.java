package aria1th.main.matchrevisions.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Nullable public ClientPlayerEntity player;

	@Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	private void onKeyPressed(CallbackInfo ci){
		ItemStack mainStack = this.player.getMainHandStack();
		this.player.setStackInHand(Hand.MAIN_HAND, this.player.getStackInHand(Hand.OFF_HAND));
		this.player.setStackInHand(Hand.OFF_HAND, mainStack);
		this.player.currentScreenHandler.nextRevision();
		this.player.currentScreenHandler.nextRevision();
	}
}