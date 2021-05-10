package de.projektlabor.makiekillerbot.cac.game.player.packets.server;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.logic.Controller;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

/**
 * Sends the current game-controller and the time until he is allowed to controll
 * @author Noah
 *
 */

public class SPlayerGameController implements IPacketServer<Player>{

	// The controller
	private Controller controller;
	
	public SPlayerGameController(Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public void writePacketData(JSONObject packet) {
		packet.put("uuid",this.controller.getPlayer().getUUID());
		packet.put("until", this.controller.getControllerUntil());
	}

}
