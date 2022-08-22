package aria1th.main.matchrevisions.mixins;

import aria1th.main.matchrevisions.utils.FakeCommandExecutor;
import com.mojang.brigadier.StringReader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
	@Inject(method = "sendCommand(Ljava/lang/String;Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
	private void checkClientCommand(String command, Text preview, CallbackInfo ci){
		StringReader reader = new StringReader(command);
		int cursor = reader.getCursor();
		String commandName = reader.canRead() ? reader.readUnquotedString() : "";
		if (commandName.contains("matchrevision")){
			reader.setCursor(cursor);
			FakeCommandExecutor.executeCommand(reader);
			ci.cancel();
		}
	}
}