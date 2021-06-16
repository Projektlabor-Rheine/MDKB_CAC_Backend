package de.projektlabor.makiekillerbot.cac.game.pi.packets.client;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketClient;
import de.projektlabor.makiekillerbot.cac.game.pi.RaspberryPi;

public class CPiLinedetection implements IPacketClient<RaspberryPi>{

	// If a line got detected
	public boolean isLineDetected;
	
	public CPiLinedetection() {}
	
	@Override
	public void readPacketData(JSONObject packet) throws Exception {
		this.isLineDetected = packet.getBoolean("on");
	}

}
