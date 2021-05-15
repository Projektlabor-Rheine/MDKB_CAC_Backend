package de.projektlabor.makiekillerbot.cac.config.loading;

public class ConfigLoadException extends Exception{
	private static final long serialVersionUID = -7869625312409573730L;

	// The config-section that had an error
	private String sectionName;
	
	// The sub-exception
	private Exception subException;
	
	public ConfigLoadException(String sectionName,Exception e) {
		this.sectionName = sectionName;
		this.subException = e;
	}
	
	public String getSectionName() {
		return this.sectionName;
	}
	public Exception getSubException() {
		return this.subException;
	}
	
}
