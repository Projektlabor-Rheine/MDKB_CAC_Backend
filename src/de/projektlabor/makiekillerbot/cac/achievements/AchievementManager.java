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
import de.projektlabor.makiekillerbot.cac.util.QRCode;
import spark.Request;
import spark.Response;
import spark.Spark;

public class AchievementManager extends SubConfig{

	// List with all loaded achievements
	private List<Achievement> loadedAchievements = Collections.synchronizedList(new ArrayList<Achievement>());

	// Reference to the game
	private Game game;
	
	// Default qr-code for non-existing codes
	private QRCode defaultQRCode;
	
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
	 * Used to unlock a achievement. Will redirect back to the main page after unlocking
	 */
	public Object onRequestUnlock(Request request, Response response) {
		
		// Gets the achievement-id
		String avmtId = request.params("id");
		
		// Tries to find the achievement with that id
		Optional<Achievement> optAvmt = this.loadedAchievements.stream().filter(i->i.getUnlockId().equals(avmtId)).findAny();
		
		// QR-Code that the user will be redirected to
		QRCode code = this.defaultQRCode;
		
		// Checks if the achievement exists
		if(optAvmt.isPresent()) {
			
			// Gets the achievement
			Achievement avmt = optAvmt.get();

			// Updates the qr-code
			code = optAvmt.get().getQRCode();
			
			// Checks if the achievement hasn't been found yet
			if(!avmt.hasBeenFound()) {				
				// Updates the achievement
				avmt.setHasBeenFound(true);
				
				// Saves the config
				this.game.getConfig().saveIgnoreErrors();
				
				// Executes the event
				this.game.onAchievementFound(avmt);
			}
			
		}
		
		// Redirects to the code's content
		response.redirect(code.getRawContent());
		return "";
	}

	/**
	 * Used to access the qr-code with it's corresponding id
	 */
	public Object onRequestAccess(Request request, Response response) {
		
		// Sets the response-type to an png-image
		response.type("image/png");
		
		// Gets the achievement-id
		String avmtId = request.params("id");
		
		// Tries to find the achievement with that id
		Optional<Achievement> optAvmt = this.loadedAchievements.stream().filter(i->i.getUnlockId().equals(avmtId)).findAny();
		
		// Checks if eigther the achievement couldn't be found or if it still is hidden
		if(!optAvmt.isPresent() || !optAvmt.get().hasBeenFound())
			// Returns the default-qr-code
			return this.defaultQRCode.getImageData();
		
		// Returns the actual qr-code data
		return optAvmt.get().getQRCode().getImageData();
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
							put("qrcodelink",i.getQRCode().getRawContent());
						}
					};
				})
				// Collects them as a json-array
				.collect(JSONUtils.JSON_ARRAY_COLLECTOR);
		
		// Appends the achievement-array
		saveTo.put("list", achievementlist);
		
		// Appends the default qr-code
		saveTo.put("default-qr-link", this.defaultQRCode.getRawContent());
	}

	@Override
	public void loadConfig(JSONObject loadFrom) throws Exception {
		// Clears any previously loaded achievements
		this.loadedAchievements.clear();
		
		// Gets the achievement array
		JSONArray storedAchievements = loadFrom.getJSONArray("list");
		
		// Loads the default qr-code
		this.defaultQRCode = QRCode.createQRCodeBytesFromString(loadFrom.getString("default-qr-link"), 200, 200);
		
		// Starts to parse the achievements
		for(int i=0;i<storedAchievements.length();i++) {
			// Gets the object
			JSONObject ach = storedAchievements.getJSONObject(i);
			
			// Creates the qr-code
			QRCode code = QRCode.createQRCodeBytesFromString(ach.getString("qrcodelink"), 200, 200);
			
			// Loads the achievement
			Achievement achievement = new Achievement(i,ach.getString("hint"), ach.getString("unlockid"), code, ach.getBoolean("unlocked"));
			
			// Adds it to the list
			this.loadedAchievements.add(achievement);
		}
	}

	@Override
	public void loadExampleConfig() {
		// Loads a default achievement and clears an previous ones
		this.loadedAchievements.clear();
		try {
			
			// Creates the example qr-code
			QRCode code = QRCode.createQRCodeBytesFromString("https://www.youtube.com/watch?v=2942BB1JXFk", 200, 200);
			
			// Adds a example achievement
			this.loadedAchievements.add(new Achievement(0,"Example-achievement", "1337", code, false));
			
			// Creates the default qr-code
			this.defaultQRCode = QRCode.createQRCodeBytesFromString("https://www.youtube.com/watch?v=dQw4w9WgXcQ", 200, 200);
		} catch (Exception e) {
			// Very critical error occurred
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
