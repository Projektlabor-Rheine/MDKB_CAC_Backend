package de.projektlabor.makiekillerbot.cac.game.player.packets.server;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

/**
 * Sends the player's init packet
 * @author Noah
 *
 */

public class SPlayerInit implements IPacketServer<Player> {

	// The current game
	private Game game;
	
	// The profile of the player
	private Player profile;
	
	
	public SPlayerInit(Game game,Player profile) {
		this.game=game;
		this.profile=profile;
	}
	
	@Override
	public void writePacketData(JSONObject packet) {
		// Writes the game's players
		new SPlayerGamePlayers(this.game.getPlayers()).writePacketData(packet);
		// Writes the games achievements
		new SPlayerGameAchievements(this.game.getAchievementManager().getLoadedAchievements()).writePacketData(packet);
		// Writes the game-controller
		new SPlayerGameController(this.game.getController()).writePacketData(packet);
		
		// Writes the players profile
		packet.put("profile", this.convertPlayerToProfile(this.profile));
	}
	
	/**
	 * Converts a given player object to an json-object that contains all profile details
	 * @param p the player
	 */
	private JSONObject convertPlayerToProfile(Player p) {
		JSONObject obj = new JSONObject();
		obj.put("uuid", p.getUUID());
		obj.put("pos", p.getQueueIndex());
		obj.put("name", p.getUsername());
		return obj;
	}
	
}
