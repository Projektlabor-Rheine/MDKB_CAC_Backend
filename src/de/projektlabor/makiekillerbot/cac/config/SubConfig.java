package de.projektlabor.makiekillerbot.cac.config;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.config.loading.ConfigLoadValueException;
import de.projektlabor.makiekillerbot.cac.config.loading.Loadable;
import de.projektlabor.makiekillerbot.cac.config.loading.Loader;

public abstract class SubConfig {

	// If reflected config loading is enabled inside the config. This will scan the
	// whole class for ConfigLoadable annotated fields and saved/loads them
	private boolean enableReflectedConfig = false;

	// The config that this subconfig is associated with
	private Config parent;
	
	/**
	 * Saves the current state of the subconfig to the given object. On the next
	 * load this object will be supplied to loadConfig
	 */
	public void saveConfig(JSONObject saveTo) {
		// Checks if reflected config-loading is enabled
		if (this.enableReflectedConfig)
			this.saveRefelectedConfig(saveTo);
	}

	/**
	 * Searches for all reflectable field inside the class
	 */
	private Field[] getAllReflectedFields() {
		return Arrays.stream(this.getClass().getDeclaredFields())
				// Checks if the field is a config-loadable
				.filter(i -> i.isAnnotationPresent(Loadable.class))
				// Collects all
				.toArray(Field[]::new);
	}

	/**
	 * Load all values from the given loadFrom object. This is the object that got
	 * passed saveConfig as well
	 * 
	 * @throws Exception
	 *             if anything went wrong. This way it will display an error to the
	 *             user
	 */
	public void loadConfig(JSONObject loadFrom) throws Exception{
		// Checks if reflected config-loading is enabled
		if (this.enableReflectedConfig)
			this.loadReflectedConfig(loadFrom);
	}

	/**
	 * If no config has been loaded at this point, this shall supply an example
	 * config for the user to oriantate at
	 */
	public abstract void loadExampleConfig();

	/**
	 * Uses refelection to find and load config-values
	 * @param loadFrom the json-object from which the values should be loaded
	 * @throws Exception if any value hasn't bee given or was invalid
	 */
	@SuppressWarnings({ "rawtypes" })
	private void loadReflectedConfig(JSONObject loadFrom) throws Exception{
		try {
			// Gets all refelectable fields
			for(Field f : this.getAllReflectedFields()) {
				// Makes the field accessible
				f.setAccessible(true);
				
				// Gets the configloadable
				Loadable cfgload = f.getDeclaredAnnotation(Loadable.class);
				
				// Gets the loader for the value
				Loader loader = this.parent.getLoaderByClass(cfgload.loader());
				
				// The final loaded value
				Object value = null;
				
				try {					
					value = loader.loadValue(loadFrom.getString(cfgload.name()));
				}catch(Exception e) {
					throw new ConfigLoadValueException();
				}
				
				// Sets the value
				f.set(this, value);
			}
		}catch(JSONException|ConfigLoadValueException e) {
			throw e;
		} catch(Exception e) {
			// This should not happen at the final application as this is just a config-thing
			// TODO: Debug remove
			e.printStackTrace();
		}
	}
	
	/**
	 * Uses refelection to find and save config-values
	 * @param saveTo the object that they shall be saved to
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void saveRefelectedConfig(JSONObject saveTo) {
		try {				
			// Gets all refelectable fields
			for(Field f : this.getAllReflectedFields()) {
				// Makes the field accessible
				f.setAccessible(true);
				
				// Gets the fields value
				Object val = f.get(this);
				
				// Gets the configloadable
				Loadable cfgload = f.getDeclaredAnnotation(Loadable.class);
				
				// Gets the loader for the value
				Loader loader = this.parent.getLoaderByClass(cfgload.loader());
				
				// Saves the value to the file
				saveTo.put(cfgload.name(), loader.saveValue(val));
			}
		}catch(Exception e) {
			// This should not happen at the final application as this is just a config-thing
			// TODO: Debug remove
			e.printStackTrace();
		}
	}
	
	public void setParent(Config parent) {
		this.parent = parent;
	}
	public void enableReflectedConfig() {
		this.enableReflectedConfig=true;
	}
}
