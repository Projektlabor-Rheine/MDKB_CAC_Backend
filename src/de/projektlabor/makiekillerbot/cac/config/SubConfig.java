package de.projektlabor.makiekillerbot.cac.config;

import org.json.JSONObject;

public interface SubConfig {

	/**
	 * Saves the current state of the subconfig to the given object.
	 * On the next load this object will be supplied to loadConfig
	 */
	public void saveConfig(JSONObject saveTo);
	
	/**
	 * Load all values from the given loadFrom object. This is the object that got passed saveConfig as well
	 * @throws Exception if anything went wrong. This way it will display an error to the user
	 */
	public void loadConfig(JSONObject loadFrom) throws Exception;
	
	/**
	 * If no config has been loaded at this point, this shall supply an example config for the user to oriantate at
	 */
	public void loadExampleConfig();
	
}
