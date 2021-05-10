package de.projektlabor.makiekillerbot.cac.game.player.packets.server;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.achievements.Achievement;
import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.player.Player;
import de.projektlabor.makiekillerbot.cac.util.JSONUtils;

/**
 * Sends all found achievements and the code if they have been unlocked
 * @author Noah
 *
 */

public class SPlayerGameAchievements implements IPacketServer<Player>{

	// All existing achievements
	private List<Achievement> achievements;
	
	public SPlayerGameAchievements(List<Achievement> achievements) {
		this.achievements=achievements;
	}
	
	@Override
	public void writePacketData(JSONObject packet) {
		// Gets all achievements, converts them to json-objects and collected them as a json-array
		JSONArray arr = this.achievements.stream().map(this::achievementToJSON).collect(JSONUtils.JSON_ARRAY_COLLECTOR);
		
		// Appends the achievements
		packet.put("list", arr);
	}
	
	/**
	 * Converts the given achievement to a json-object
	 */
	private JSONObject achievementToJSON(Achievement avmt) {
		return new JSONObject() {{
			put("name",avmt.getHintName());
			put("unlocked",avmt.hasBeenFound());
			if(avmt.hasBeenFound())
				put("code",avmt.getUnlockId());
		}};
	}

}
