package de.projektlabor.makiekillerbot.cac.game.player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.config.SubConfig;

public class PlayerConfig extends SubConfig{

	// Contains all loaded valid usernames
	private String[] loadedNames;
	
	public String getRandomPlayerName() {
		return this.loadedNames[ThreadLocalRandom.current().nextInt(this.loadedNames.length)];
	}
	
	@Override
	public void saveConfig(JSONObject saveTo) {
		saveTo.put("names", this.loadedNames);
	}

	@Override
	public void loadConfig(JSONObject loadFrom) throws Exception {
		// Loads the names
		this.loadedNames = StreamSupport.stream(loadFrom.getJSONArray("names").spliterator(),false).map(i->(String)i).toArray(String[]::new);
	}

	@Override
	public void loadExampleConfig() {
		this.loadedNames = new String[] {"Alan Turing","Albert Einstein"};
	}

}
