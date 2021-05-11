package de.projektlabor.makiekillerbot.cac.game.player.packets.client;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketClient;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

/**
 * Request by the client to resend his init-packet as it may got lost on the way
 * @author Noah
 *
 */

public class CPlayerInitReqeust implements IPacketClient<Player>{	
	@Override
	public void readPacketData(JSONObject packet) throws Exception {}
}
