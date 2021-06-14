package de.projektlabor.makiekillerbot.cac.game.pi.packets.server;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.pi.RaspberryPi;
import de.projektlabor.makiekillerbot.cac.game.player.packets.client.CPlayerControllsupdate;

public class SPiControllsupdate implements IPacketServer<RaspberryPi>{

	// The pressed status of all keys. Uses the indexes in CPlayerControllsupdate
	public boolean[] pressedKeys = new boolean[CPlayerControllsupdate.KEYS.length];
	
	// Creates a packet with all keys disabled
	public SPiControllsupdate() {
		this.pressedKeys = new boolean[CPlayerControllsupdate.KEYS.length];
	}
	
	public SPiControllsupdate(boolean... keys) {
		this.pressedKeys = keys;
	}
	
	public SPiControllsupdate(CPlayerControllsupdate forward) {
		this.pressedKeys = forward.pressedKeys.clone();
	}
	
	@Override
	public void writePacketData(JSONObject packet) {
		// Appends the keys
		for(int i=0;i<this.pressedKeys.length;i++)
			packet.put(CPlayerControllsupdate.KEYS[i], this.pressedKeys[i]);
	}

}
