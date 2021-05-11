package de.projektlabor.makiekillerbot.cac.game.pi;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import de.projektlabor.makiekillerbot.cac.connection.Nethandler;
import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.Game;

@WebSocket
public class NethandlerPi extends Nethandler<RaspberryPi>{

	// Reference to the pi
	private RaspberryPi pi;

	// Reference to the game
	private Game logic;
	
	/**
	 * @param game reference to the main game
	 */
	public NethandlerPi(Game game) {
		this.logic=game;
		this.pi=game.getRaspberrypi();
		
		this.initNethandler();
	}

	@Override
	public void onConnect(Session session) {
		// Checks if there is already a pi connected
		if(this.pi.getConnection() != null) {
			// Disconnects the new user
			try {
				session.disconnect();
			} catch (IOException e) {}
			return;
		}
		
		// Updates the pi connection
		this.pi.updateConnection(session);
		
		// Executes the event
		this.logic.onRaspiConnect();
	}

	@Override
	public void onDisconnect(Session session, int statusCode, String reason) {
		// Updates the connection
		this.pi.updateConnection(null);

		// Executes the event
		this.logic.onRaspiDisconnect();
	}


	@Override
	public Map<Integer, Entry<Class<?>, BiConsumer<RaspberryPi, Object>>> registerClientPackets() {
		// TODO Implement pi-packets
		return null;
	}

	@Override
	public Map<Class<? extends IPacketServer<RaspberryPi>>, Integer> registerServerPackets() {
		// TODO Implement pi-packets
		return null;
	}

	@Override
	public RaspberryPi getCorrespondinConnection(Session session) {
		return this.pi;
	}
}
