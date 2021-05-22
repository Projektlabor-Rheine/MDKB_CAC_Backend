package de.projektlabor.makiekillerbot.cac.util;

public class Timer {

	// Last time the timer reseted
	private long executeUnixTime;
	
	public Timer() {
		this.reset();
	}
	
	/**
	 * Resets the timer
	 */
	public void reset() {
		// Resets the timer
		this.executeUnixTime = System.currentTimeMillis();
	}
	
	/**
	 * Checks if the timer has reached the given amount of millis
	 * @param millis the amount of millis that are needed to be reached
	 */
	public boolean hasReached(long millis) {
		return this.getConnectedTime() > millis;
	}
	
	/**
	 * Checks if the timer has reached the given amount of millis. If he has, the timer resets itself
	 * 
	 * @return if the timer has reached the amount of millis
	 */
	public boolean hasReachedIfReset(long millis) {
		// Checks if the amount has been reached
		if(this.hasReached(millis)) {
			this.reset();
			return true;
		}
		return false;
	}
	
	public long getStartTimeUnix() {
		return this.executeUnixTime;
	}
	
	public long getConnectedTime() {
		return System.currentTimeMillis()-this.executeUnixTime;
	}
}
