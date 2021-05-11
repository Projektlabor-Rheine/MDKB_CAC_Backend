package de.projektlabor.makiekillerbot.cac.game.pi;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;

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
	
	// Reference to the nethandler
	private final NethandlerPi nethandler;
	
	public RaspberryPi(NethandlerPi nethandler) {
		this.nethandler = nethandler;
	}
	
	
	/**
	 * Sends the given raw packet to the pi.
	 * Sends the packet async
	 * 
	 * @param pkt the packet to send
	 */
	public void sendRawPacket(String pkt) {
		// Tries to deliver the packet
		this.connection.getRemote().sendStringByFuture(pkt);
	}

	/**
	 * Sends the packet to the pi
	 * 
	 * @param packet
	 *            the packet to send to the pi
	 */
	public void sendPacket(IPacketServer<RaspberryPi> packet) {
		// Gets the final packet
		JSONObject finPkt = this.nethandler.getFinalizedPacket(packet);

		// Sends the raw packet-data
		this.sendRawPacket(finPkt.toString());
	}
	
	
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
