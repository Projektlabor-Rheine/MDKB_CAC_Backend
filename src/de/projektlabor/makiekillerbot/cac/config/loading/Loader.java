package de.projektlabor.makiekillerbot.cac.config.loading;

public interface Loader<T>{

	/**
	 * Used to save the value as a string that can later be loaded again.
	 * Saves the value into a human readable string as it shall be editable as a config file later on
	 * @param obj the raw object that shall be saved
	 */
	public String saveValue(T obj);

	/**
	 * Used to load the object back from the saved string by saveValue
	 * @param value the raw string to load from
	 * @throws Exception if the string is not in the expected format
	 */
	public T loadValue(String value) throws Exception;
	
}
