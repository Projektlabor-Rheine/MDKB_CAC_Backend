package de.projektlabor.makiekillerbot.cac.game.player.packets.server;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

/**
 * Sends if the raspi is connected
 * @author Noah
 *
 */

public class SPlayerGameRaspiStatus implements IPacketServer<Player>{

	// If the raspi is connected
	private boolean hasConnection;
	
	public SPlayerGameRaspiStatus(boolean hasConnection) {
		this.hasConnection = hasConnection;
	}

	@Override
	public void writePacketData(JSONObject packet) {
		packet.put("rpistatus", this.hasConnection);
	}
	
}
