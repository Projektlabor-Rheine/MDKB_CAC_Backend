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
import de.projektlabor.makiekillerbot.cac.game.pi.packets.client.CPiLinedetection;
import de.projektlabor.makiekillerbot.cac.game.pi.packets.server.SPiControllsupdate;

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
		// Creates the pi instance
		this.pi = new RaspberryPi(this);
		
		// Sets the references
		this.logic=game;
		game.setRaspberrypi(this.pi);
		
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
		
		// Gets the time that the pi disconnection lasted
		long lastConnectionTime = this.pi.getConnectedSince();
		
		// Updates the pi connection
		this.pi.updateConnection(session);
		
		// Executes the event
		this.logic.onRaspiConnect(lastConnectionTime);
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
		return registerC(
			registerC(0,CPiLinedetection.class,this.logic::onRaspiUpdateLinedetection)
		);
	}

	@Override
	public Map<Class<? extends IPacketServer<RaspberryPi>>, Integer> registerServerPackets() {
		return registerS(
			registerS(0,SPiControllsupdate.class)
		);
	}

	@Override
	public RaspberryPi getCorrespondinConnection(Session session) {
		return this.pi;
	}
}
