package aria1th.main.matchrevisions.utils;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class MessageHolder {
	final ClientPlayerEntity player;
	public boolean allow = true;
	public boolean useActionbar = false;
	public MessageHolder(ClientPlayerEntity player){
		this.player = player;
	}

	public void sendMessage(String message){
		if (!this.allow){
			return;
		}
		this.player.sendMessage(Text.of(message), useActionbar);
	}
	public void sendMessage(Text message){
		if (!this.allow){
			return;
		}
		this.player.sendMessage(message, useActionbar);
	}
}