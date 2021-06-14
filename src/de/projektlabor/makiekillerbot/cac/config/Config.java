package de.projektlabor.makiekillerbot.cac.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.config.loading.ConfigLoadException;
import de.projektlabor.makiekillerbot.cac.config.loading.Loader;
import de.projektlabor.makiekillerbot.cac.config.loading.loaders.IntLoader;
import de.projektlabor.makiekillerbot.cac.config.loading.loaders.Longloader;
import de.projektlabor.makiekillerbot.cac.config.loading.loaders.Stringloader;

public class Config {

	// List with subconfigs that must be loaded
	// String (Value that the subconfig will be saved as on the json-object) -
	// SubConfig (The actual config)
	public final Map<String, SubConfig> subconfigs = new HashMap<String, SubConfig>();
	
	// The config file
	private final File configFile;

	// Holds instances for all config-loaders
	private final Set<Loader<?>> configLoader = new HashSet<>();

	public Config(File cfgFile) {
		this.configFile = cfgFile;
		
		// Registers some default loaders
		this.registerConfigLoader(new Longloader());
		this.registerConfigLoader(new Stringloader());
		this.registerConfigLoader(new IntLoader());
	}
	
	/**
	 * Registers a config-loader for the config
	 * @param loader the loader to register
	 */
	public void registerConfigLoader(Loader<?> loader) {
		this.configLoader.add(loader);
	}

	/**
	 * Registers the given subconfig
	 * 
	 * @param tag
	 *            the tag that shall be used to identify the config on the
	 *            json-object
	 * @param sc
	 *            the actual subconfig
	 */
	public void registerSubconfig(String tag, SubConfig sc) {
		// Updates the parent
		sc.setParent(this);
		
		// Appends to the configs
		this.subconfigs.put(tag, sc);
	}

	/**
	 * Tries to load the config from the file
	 * 
	 * @exception IOException
	 *                if anything went wrong while requesting the file
	 * @exception ConfigLoadException if a subconfig failed to load. Contains the section and the inital error
	 */
	public void load() throws IOException,ConfigLoadException {

		// Will hold the fully loaded config-object
		JSONObject configObject;

		// Checks if the config-file doesn't exists
		if (!this.configFile.exists()) {
			// Ensures that the file-directory exists
			if (this.configFile.getParentFile() != null)
				this.configFile.getParentFile().mkdirs();

			// Creates an empty config
			configObject = new JSONObject();
		} else {
			// Loads the config as a utf-8 file
			configObject = new JSONObject(
					new String(Files.readAllBytes(this.configFile.toPath()), StandardCharsets.UTF_8));
		}

		// Iterates over all subconfigs and loads them
		for (Entry<String, SubConfig> sc : this.subconfigs.entrySet()) {
			// Checks if the value exists
			if(configObject.has(sc.getKey())) {
				
				// Trys to load the config
				try {
					sc.getValue().loadConfig(configObject.getJSONObject(sc.getKey()));
				}catch(Exception e) {
					// Forwards the exception with the section-tag
					throw new ConfigLoadException(sc.getKey(), e);
				}
			}else
				// Load the example-config
				sc.getValue().loadExampleConfig();
		}
		
		// All subconfig have loaded successfully, updates the file
		this.save();
	}

	/**
	 * Saves the loaded config to the config-file
	 * @throws IOException if anything went wrong with the i/o
	 */
	public void save() throws IOException{
		// The save-object
		JSONObject obj = new JSONObject();
		
		// Iterates over all subconfigs and loads them
		for (Entry<String, SubConfig> sc : this.subconfigs.entrySet()) {
			// Creates the entry for the subconfig
			JSONObject entry = new JSONObject();
			
			// Saves the values
			sc.getValue().saveConfig(entry);
			
			// Appends the subconfig
			obj.put(sc.getKey(), entry);
		}
		
		// Stores the config using utf-8
		Files.write(this.configFile.toPath(), obj.toString(1).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Saves the current config, but ignores all errors
	 */
	public void saveIgnoreErrors() {
		try {
			this.save();
		}catch(Exception e) {
			// TODO Debug: Remove
			e.printStackTrace();
		}
	}
	
	/**
	 * Searches a config-loader by it's class.
	 * This method is unsafe it will throw an exception if the config hasn't been configured correctly
	 * @param clazz the class to search for
	 * @return the config loader that corresponds to the given class
	 */
	public Loader<?> getLoaderByClass(Class<? extends Loader<?>> clazz){
		return this.configLoader.stream().filter(i->i.getClass().equals(clazz)).findAny().get();
	}
	
}
