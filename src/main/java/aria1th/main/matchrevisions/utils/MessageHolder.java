package aria1th.main.matchrevisions.utils;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class MessageHolder {
	final ClientPlayerEntity player;
	public static boolean allow = false;
	public static boolean useActionbar = false;
	public MessageHolder(ClientPlayerEntity player){
		this.player = player;
	}

	public void sendMessage(String message){
		if (!allow){
			return;
		}
		this.player.sendMessage(Text.of(message), useActionbar);
	}
	public void sendMessage(Text message){
		if (!allow){
			return;
		}
		this.player.sendMessage(message, useActionbar);
	}
}