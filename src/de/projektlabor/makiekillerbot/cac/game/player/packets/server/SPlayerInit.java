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
		this.appendPacketUnderName(packet, null, new SPlayerGamePlayers(this.game.getPlayers()));
		// Writes the games achievements
		this.appendPacketUnderName(packet, null, new SPlayerGameAchievements(this.game.getAchievementManager().getLoadedAchievements()));
		// Writes the game-controller
		this.appendPacketUnderName(packet, null, new SPlayerGameController(this.game.getController()));
		// Writes the current rpi-status
		this.appendPacketUnderName(packet, null, new SPlayerGameRaspiStatus(this.game.getRaspberrypi().isConnected()));
		
		// Writes the players profile
		packet.put("profile", this.convertPlayerToProfile(this.profile));
	}
	
	/**
	 * Appends the given packet under the name to the parentPacket.
	 * If the name is not given, it just writes the whole packet-content into the parent
	 */
	private void appendPacketUnderName(JSONObject parentPacket,String name,IPacketServer<Player> packet) {
		if(name == null)
			packet.writePacketData(parentPacket);
		else {			
			JSONObject pktObj = new JSONObject();
			packet.writePacketData(pktObj);
			parentPacket.put(name, pktObj);
		}
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
		obj.put("color", p.getColor());
		return obj;
	}
	
}
