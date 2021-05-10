package de.projektlabor.makiekillerbot.cac.raspi;

import org.eclipse.jetty.websocket.api.Session;

/**
 * This is the raspberry pi controller class.
 * It is used to handle basic function of the pi or store it's properties
 * @author Noah
 *
 */

public class RaspberryPi {

	// Connection of the pi
	// Can be null if the pi is not connected
	private Session connection;
	
	// Since when the raspi is connected (or disconnected)
	private long connectedSince;
	
	public long getConnectionTimestamp() {
		return this.connectedSince;
	}
	
	public Session getConnection() {
		return this.connection;
	}
	
	public boolean isConnected() {
		return this.connection != null;
	}
	
	public long getConnectedSince() {
		return this.connectedSince;
	}
	
	public void updateConnection(Session connection) {
		this.connection = connection;
		this.connectedSince = System.currentTimeMillis();
	}
}
