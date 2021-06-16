package de.projektlabor.makiekillerbot.cac.game.player.packets.server;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

public class SPlayerLineDetected implements IPacketServer<Player>{

	// If a line got detected
	public boolean isLineDetected;
	
	public SPlayerLineDetected(boolean isLineDetected) {
		this.isLineDetected = isLineDetected;
	}
	
	@Override
	public void writePacketData(JSONObject packet) {
		packet.put("on", packet);
	}

}
