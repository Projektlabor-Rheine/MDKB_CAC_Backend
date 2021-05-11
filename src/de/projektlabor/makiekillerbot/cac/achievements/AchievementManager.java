package de.projektlabor.makiekillerbot.cac.achievements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.config.SubConfig;
import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.util.JSONUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

public class AchievementManager implements SubConfig{

	// List with all loaded achievements
	private List<Achievement> loadedAchievements = Collections.synchronizedList(new ArrayList<Achievement>());

	// Reference to the game
	private Game game;
	
	/**
	 * Finishes the initialization of the achievement-manager
	 * Should be called after everything has been setup for the webserver
	 */
	public void finishInit(String webpath) {
		// Registers the webpath's to unlock and access
		Spark.get(webpath+"/unlock/:id", this::onRequestUnlock);
		Spark.get(webpath+"/access/:id", this::onRequestAccess);
	}

	/**
	 * Executes when someone sends a unlock request
	 */
	public Object onRequestUnlock(Request request, Response response) {
		
		// Gets the achievement-id
		String avmtId = request.params("id");
		
		// Tries to find the achievement with that id
		Optional<Achievement> optAvmt = this.loadedAchievements.stream().filter(i->i.getUnlockId().equals(avmtId)).findAny();
				
		// Checks if the achievement exists and hasn't been found
		if(optAvmt.isPresent() && !optAvmt.get().hasBeenFound()) {
			// Gets the achievement
			Achievement avmt = optAvmt.get();
			
			// Updates the achievement
			avmt.setHasBeenFound(true);
			
			// Saves the config
			this.game.getConfig().saveIgnoreErrors();;
			
			// Executes the event
			this.game.onAchievementFound(avmt);
		}
		
		// Redirects to the main page
		// TODO: Make better page to redirect to
		response.redirect("/");
		return "";
	}

	/**
	 * Executes when someone sends a access request for an achievement
	 * @param request
	 * @param response
	 * @return
	 */
	public Object onRequestAccess(Request request, Response response) {
		// Gets the achievement-id
		String avmtId = request.params("id");
		
		// Tries to find the achievement with that id
		Optional<Achievement> optAvmt = this.loadedAchievements.stream().filter(i->i.getUnlockId().equals(avmtId)).findAny();
		
		// TODO: Implement better responses
		
		// Checks if eigther the achievement couldn't be found or if it still is hidden
		if(!optAvmt.isPresent() || !optAvmt.get().hasBeenFound())
			return "Not found";
		
		return "Found";
	}
	

	public void setGameReference(Game game) {
		this.game = game;
	}
	public List<Achievement> getLoadedAchievements() {
		return this.loadedAchievements;
	}

	@Override
	public void saveConfig(JSONObject saveTo) {
		// Converts all loaded achievements into a json-array
		JSONArray achievementlist = this.loadedAchievements.stream()
				// Maps all achivements to json-objects
				.map(i -> {
					return new JSONObject() {
						{
							put("hint", i.getHintName());
							put("unlockid", i.getUnlockId());
							put("unlocked", i.hasBeenFound());
						}
					};
				})
				// Collects them as a json-array
				.collect(JSONUtils.JSON_ARRAY_COLLECTOR);
		
		// Appends the achievement-array
		saveTo.put("list", achievementlist);
	}

	@Override
	public void loadConfig(JSONObject loadFrom) throws Exception {
		// Clears any previously loaded achievements
		this.loadedAchievements.clear();
		
		// Gets the achievement array
		JSONArray storedAchievements = loadFrom.getJSONArray("list");
		
		// Starts to parse the achievements
		for(int i=0;i<storedAchievements.length();i++) {
			// Gets the object
			JSONObject ach = storedAchievements.getJSONObject(i);
			
			// Loads the achievement
			Achievement achievement = new Achievement(ach.getString("hint"), ach.getString("unlockid"), ach.getBoolean("unlocked"));
			
			// Adds it to the list
			this.loadedAchievements.add(achievement);
		}
	}

	@Override
	public void loadExampleConfig() {
		// Loads a default achievement and clears an previous ones
		this.loadedAchievements.clear();
		this.loadedAchievements.add(new Achievement("Example-achievement", "1337", false));
	}
}
