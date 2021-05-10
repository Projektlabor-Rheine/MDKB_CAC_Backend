package de.projektlabor.makiekillerbot.cac.connection;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketClient;
import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.player.Player;
import de.projektlabor.makiekillerbot.cac.util.Triple;

@WebSocket
public abstract class Nethandler<Type> {

	// Holds all packets that can be received. If a packet gets detected that is not
	// registered in this map, it will be ignored
	// Integer - the unique id of the packet. Used to identifie packets on both
	// sides of the socket
	// Class<Packet> - The class with the packet that can be parsed from the
	// received data
	// BiConsumer<Packet> - The handler that shall be excuted when this packet get's
	// received.
	private Map<Integer, Entry<Class<?>, BiConsumer<Type,Object>>> clientPackets;

	// Holds all packets that can be send. If a packet gets detected that is not
	// registered in this map, it will be aborted
	// Class<Packet> - the packet that shall be send
	// Integer - the unique id of the packet. Used to identifie packets on both
	// sides of the socket
	private Map<Class<? extends IPacketServer<Type>>, Integer/* id */> serverPackets;

	/**
	 * Used to init and register all packets run after the constructor in the inherited class
	 */
	protected void initNethandler() {
		this.clientPackets = this.registerClientPackets();
		this.serverPackets = this.registerServerPackets();
	}
	
	/**
	 * Finalizes the given client-packet and converts it to a json-object that can
	 * be interpreted by the other side
	 * 
	 * @param packet
	 *            the packet that shall be finalized
	 * @exception RuntimeException
	 *                if the packet is not registered
	 * @return null if the packet is not registered (Should not happen); normaly the
	 *         finalized packet as a json-object
	 */
	public JSONObject getFinalizedPacket(IPacketServer<Type> packet) {
		// Searches the packet-id by the class-type
		int pktId = this.serverPackets.getOrDefault(packet.getClass(), -1);

		// Checks if the packet is invalid (No packet-id is given)
		if (pktId < 0)
			// Throws a runtime exception
			throw new RuntimeException("Detected unregistered packet: " + packet.getClass().getSimpleName());

		// Gets the packet-data
		JSONObject data = new JSONObject();
		// Appends the data
		packet.writePacketData(data);

		// Creates the wrapper for the packet and converts the final packet to json
		return new JSONObject() {
			{
				put("id", pktId);
				put("data", data);
			}
		};
	}

	/**
	 * Used to register all server packets (and their handlers) once
	 * 
	 * @return
	 */
	public abstract Map<Integer, Entry<Class<?>, BiConsumer<Type,Object>>> registerClientPackets();

	/**
	 * Used to register all client packets once
	 */
	public abstract Map<Class<? extends IPacketServer<Type>>, Integer> registerServerPackets();

	/**
	 * Used to find the corresponding connection for a given session
	 */
	public abstract Type getCorrespondinConnection(Session session);
	
	@OnWebSocketConnect
	public abstract void onConnect(Session session);

	@OnWebSocketClose
	public abstract void onDisconnect(Session session, int statusCode, String reason);

	@SuppressWarnings("unchecked")
	@OnWebSocketMessage
	public <T extends IPacketClient<Type>> void onMessage(Session session, String message) {
		
		// Gets the corresponding type connection for the message
		Type con = this.getCorrespondinConnection(session);
		
		try {
			// Parses the message to json
			JSONObject pkt = new JSONObject(message);
						
			// Gets the packet-handler
			Entry<Class<?>, BiConsumer<Type, Object>> pktHandler = this.clientPackets
					.get(pkt.getInt("id"));

			// Creates a new class with the packet-handler
			IPacketClient<Type> packet = (IPacketClient<Type>) pktHandler.getKey().newInstance();

			// Reads in the packet data
			packet.readPacketData(pkt);
			// Executes handler handler for that packet
			pktHandler.getValue().accept(con, (T) packet);
		} catch (Exception e) {
			// TODO Debug: Remove
			e.printStackTrace();
			// Ignores all invalid packets
		}

	}

	// TODO Debug: Remove
	@OnWebSocketError
	public void error(Throwable e) {
		System.out.println("Bad error");
		e.printStackTrace();
	}

	/**
	 * 
	 * We do NOT talk about the next four methods. Those are just code to simplify
	 * the registration process of packets
	 * 
	 */

	@SuppressWarnings("unchecked")
	protected final <T extends IPacketServer<Player>> Triple<Integer, Class<?>, BiConsumer<Player,Object>> registerX(
			Integer id, Class<?> packetType, BiConsumer<Player,T> handler) {
		 return new Triple<Integer, Class<?>, BiConsumer<Player,Object>>(id, packetType, (a,b)->handler.accept(a, (T)b));
	}

	protected final Entry<Class<? extends IPacketServer<Player>>, Integer> register(Integer id,
			Class<? extends IPacketServer<Player>> packetType) {
		return new AbstractMap.SimpleEntry<>(packetType, id);
	}

	@SuppressWarnings("serial")
	@SafeVarargs
	protected final Map<Class<? extends IPacketServer<Player>>, Integer> registerOf(
			Entry<Class<? extends IPacketServer<Player>>, Integer>... entrys) {
		return new HashMap<Class<? extends IPacketServer<Player>>, Integer>() {
			{
				// Iterates over every supplied entry and appends it to the map
				for (Entry<Class<? extends IPacketServer<Player>>, Integer> t : entrys)
					put(t.getKey(), t.getValue());
			}
		};
	}
	
	
	@SafeVarargs
	@SuppressWarnings({ "serial"})
	protected final Map<Integer, Entry<Class<?>, BiConsumer<Type,Object>>> registerOfX(Triple<Integer, Class<?>, BiConsumer<Type,Object>>... entrys){
		return new HashMap<Integer, Entry<Class<?>, BiConsumer<Type,Object>>>() {{
			for(Triple<Integer, Class<?>, BiConsumer<Type,Object>> t : entrys) {
				this.put(t.getFirst(), new SimpleEntry<Class<?>, BiConsumer<Type,Object>>(t.getSecond(),t.getThird()));
			}
		}};
	}

//	@SuppressWarnings("serial")
//	@SafeVarargs
//	protected final Map<Integer, Entry<Class<? extends IPacketClient<Type>>, BiConsumer<Session,? extends IPacketClient<Type>>>> registerOf(
//			Triple<Integer, Class<IPacketClient<Type>>, BiConsumer<Session, IPacketClient<Type>>>... entrys) {
//		return new HashMap<Integer, Map.Entry<Class<? extends IPacketClient<Type>>, BiConsumer<Session,? extends IPacketClient<Type>>>>() {
//			{
//				// Iterates over every supplied entry and appends it to the map
//				for (Triple<Integer, Class<IPacketClient<Type>>, BiConsumer<Session, IPacketClient<Type>>> t : entrys)
//					put(t.getFirst(),
//							new AbstractMap.SimpleEntry<Class<? extends IPacketClient<Type>>, BiConsumer<Session, ? extends IPacketClient<Type>>>(
//									t.getSecond(), t.getThird()));
//			}
//		};
//	}
}
