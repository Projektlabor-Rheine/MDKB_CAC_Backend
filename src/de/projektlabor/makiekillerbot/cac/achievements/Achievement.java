package de.projektlabor.makiekillerbot.cac.achievements;

public class Achievement {
	
	private String hintName;
	private String unlockId;
	private boolean hasBeenFound;
	
	public Achievement(String hintName,String unlockId,boolean hasBeenFound) {
		this.hintName=hintName;
		this.unlockId=unlockId;
		this.hasBeenFound=hasBeenFound;
	}
	
	public String getHintName() {
		return this.hintName;
	}
	public String getUnlockId() {
		return this.unlockId;
	}
	public boolean hasBeenFound() {
		return this.hasBeenFound;
	}
	public void setHasBeenFound(boolean hasBeenFound) {
		this.hasBeenFound = hasBeenFound;
	}
}
