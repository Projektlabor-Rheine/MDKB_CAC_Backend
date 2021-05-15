package de.projektlabor.makiekillerbot.cac.game.player;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.config.SubConfig;

public class PlayerConfig extends SubConfig {

	// Contains all loaded valid usernames
	private String[] loadedNames;

	// Contains all loaded valid colors
	private Integer[] loadedColors;

	public String getRandomPlayerName() {
		return this.loadedNames[ThreadLocalRandom.current().nextInt(this.loadedNames.length)];
	}

	public Integer getRandomPlayerColor() {
		return this.loadedColors[ThreadLocalRandom.current().nextInt(this.loadedColors.length)];
	}

	@Override
	public void saveConfig(JSONObject saveTo) {
		saveTo.put("names", this.loadedNames);
		saveTo.put("colors", Arrays.stream(this.loadedColors).map(Integer::toHexString).toArray(String[]::new));
	}

	@Override
	public void loadConfig(JSONObject loadFrom) throws Exception {
		// Loads the names
		this.loadedNames = StreamSupport.stream(loadFrom.getJSONArray("names").spliterator(), false)
				.map(i -> (String) i).toArray(String[]::new);
		// Loads the colors
		this.loadedColors = StreamSupport.stream(loadFrom.getJSONArray("colors").spliterator(), false)
				.map(i -> Integer.valueOf((String) i, 16)).toArray(Integer[]::new);
	}

	@Override
	public void loadExampleConfig() {
		this.loadedNames = new String[] { "Alan Turing", "Albert Einstein" };
		this.loadedColors = new Integer[] { 0xff0000, 0x00ff00, 0x0000ff, 0xffff00, 0x00ffff, 0xff00ff };
	}

}
