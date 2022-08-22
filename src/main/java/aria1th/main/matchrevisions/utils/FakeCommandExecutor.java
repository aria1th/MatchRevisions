package aria1th.main.matchrevisions.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class FakeCommandExecutor {

	public static void executeCommand(StringReader reader){
		final ClientPlayerEntity player = MinecraftClient.getInstance().player;
		try {
			player.networkHandler.getCommandDispatcher().execute(reader, new FakeCommandSource(player));
		} catch (CommandException e){
			player.sendMessage(Text.of("§c" + e.getTextMessage()));
		} catch (CommandSyntaxException e){
			player.sendMessage(Text.of("§c" + e.getMessage()));
		}
	}


	public static class FakeCommandSource extends ServerCommandSource {
		public FakeCommandSource(ClientPlayerEntity playerEntity) {
			super(playerEntity, playerEntity.getPos(), playerEntity.getRotationClient(), null, 0, playerEntity.getEntityName(), playerEntity.getName(), null, playerEntity);
		}
	}
}