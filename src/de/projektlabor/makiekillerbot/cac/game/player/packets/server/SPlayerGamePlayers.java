package de.projektlabor.makiekillerbot.cac.game.player.packets.server;

import java.util.List;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.player.Player;
import de.projektlabor.makiekillerbot.cac.util.JSONUtils;

/**
 * Sends all current players and their state's
 * @author Noah
 *
 */

public class SPlayerGamePlayers implements IPacketServer<Player> {

	// Holds all players
	private List<Player> players;
	
	public SPlayerGamePlayers(List<Player> players) {
		this.players=players;
	}
	
	@Override
	public void writePacketData(JSONObject obj) {
		// Appends all values
		obj.put("players", this.players.stream().map(this::getPlayerState).collect(JSONUtils.JSON_ARRAY_COLLECTOR));
	}

	/**
	 * Converts a simple player state to an json-object
	 * @param p the player
	 * @return the json-object
	 */
	private JSONObject getPlayerState(Player p) {
		// Creates the json-object
		JSONObject obj = new JSONObject();
		
		// Appends the players attributes
		obj.put("uuid", p.getUUID());
		obj.put("connected", p.getConnection() != null);
		obj.put("pos", p.getQueueIndex());
		obj.put("name", p.getUsername());
		if(p.getConnection() != null)
			obj.put("timer", p.getConnectionTimer().getStartTimeUnix());
		
		return obj;
	}

}
