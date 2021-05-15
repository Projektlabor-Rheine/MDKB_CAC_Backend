package de.projektlabor.makiekillerbot.cac.game.player;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.Nethandler;
import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.util.Timer;

public class Player{
	
	// The unique id of the player. Used to relogin a player after he timed out with
	// a session
	private UUID uuid;
	
	// Position where it player is in the queue
	private int queueIndex;

	// The player's username
	private String username;
	
	// The player's color
	private int color;
	
	// The websocket-connection of the player
	// Can be null if the player timed out
	private Session connection;
	
	// Nethandler for the player
	private Nethandler<Player> nethandler;
	
	// Connection-timer
	private Timer connectionTimer = new Timer();

	public Player(Nethandler<Player> nethandler, UUID uuid,String username,int color, Session connection,int queueIndex) {
		this.nethandler = nethandler;
		this.color=color;
		this.uuid = uuid;
		this.connection = connection;
		this.queueIndex=queueIndex;
		this.username=username;
	}

	/**
	 * Sends the given raw packet to the player.
	 * Sends the packet async
	 * 
	 * @param pkt the packet to send
	 */
	public void sendRawPacket(String pkt) {
		// Tries to deliver the packet
		this.connection.getRemote().sendStringByFuture(pkt);
	}

	/**
	 * Sends the packet to the player
	 * 
	 * @param packet
	 *            the packet to send to the player
	 */
	public void sendPacket(IPacketServer<Player> packet) {
		// Gets the final packet
		JSONObject finPkt = this.nethandler.getFinalizedPacket(packet);

		// Sends the raw packet-data
		this.sendRawPacket(finPkt.toString());
	}

	
	public int getQueueIndex() {
		return this.queueIndex;
	}
	public void setQueueIndex(int queueIndex) {
		this.queueIndex = queueIndex;
	}
	
	public UUID getUUID() {
		return this.uuid;
	}

	public String getUsername() {
		return this.username;
	}
	
	public int getColor() {
		return this.color;
	}
	

	public Session getConnection() {
		return this.connection;
	}

	public void setConnection(Session connection) {
		this.connection = connection;
		this.connectionTimer.reset();
	}
	
	public Timer getConnectionTimer() {
		return this.connectionTimer;
	}
}
