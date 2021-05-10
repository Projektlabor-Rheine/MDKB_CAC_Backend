package de.projektlabor.makiekillerbot.cac.achievements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.util.JSONUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

public class AchievementManager{

	// List with all loaded achievements
	private List<Achievement> loadedAchievements = Collections.synchronizedList(new ArrayList<Achievement>());
	
	// Achievement file
	private File settingsFile = new File("achievements.json");

	// Reference to the game
	private Game game;
	
	public AchievementManager() throws IOException, JSONException {
		// Loads all achievements from the file
		this.loadAchievements();
	}
	
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
	 * Loads all achievements from the file
	 * @throws IOException 
	 * @throws JSONException 
	 */
	private void loadAchievements() throws JSONException, IOException {
		// Checks if the file doesn't exists
		if(!this.settingsFile.exists()) {
			// Adds an example-achievement to the list
			this.loadedAchievements.add(new Achievement("Example-achievement", "1337", false));
			
			// Saves the achievement to the file
			this.saveAchievements();
			return;
		}
		
		// Clears any previously loaded achievements
		this.loadedAchievements.clear();
		
		// Reads all data and converts it to an json-array
		JSONArray obj = new JSONArray(new String(Files.readAllBytes(this.settingsFile.toPath()),StandardCharsets.UTF_8));
		
		// Starts to parse the achievements
		for(int i=0;i<obj.length();i++) {
			// Gets the object
			JSONObject ach = obj.getJSONObject(i);
			
			// Loads the achievement
			Achievement achievement = new Achievement(ach.getString("hint"), ach.getString("unlockid"), ach.getBoolean("unlocked"));
			
			// Adds it to the list
			this.loadedAchievements.add(achievement);
		}
	}

	/**
	 * Saves all currently loaded achievements to the specified file. Overrides any
	 * content inside that file.
	 * 
	 * @throws IOException
	 *             if anything went wrong with the i/o-writing to the file
	 */
	private void saveAchievements() throws IOException {
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

		// Writes the converted json-element to the file
		Files.write(this.settingsFile.toPath(), achievementlist.toString(1).getBytes(StandardCharsets.UTF_8));
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
			
			// Updates the file
			try {
				this.saveAchievements();
			} catch (IOException e) {
				System.out.println("Failed to save achievement list: "+e.getMessage());
			}
			
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
}
