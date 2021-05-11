package de.projektlabor.makiekillerbot.cac.game.player;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import de.projektlabor.makiekillerbot.cac.connection.Nethandler;
import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.game.player.packets.client.CPlayerInitReqeust;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerGamePlayers;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerInit;

@WebSocket
public class NethandlerPlayer extends Nethandler<Player> {

	// Reference to the game
	private final Game game;

	// Config with the loaded player settings
	private PlayerConfig pconfig;

	// Contains all current connected and timeouted players
	private final List<Player> existingPlayers = Collections.synchronizedList(new ArrayList<Player>());

	/**
	 * @param game
	 *            reference to the main game
	 */
	public NethandlerPlayer(Game game,PlayerConfig config) {
		this.pconfig = config;
		this.game = game;
		// Updates the game's connector
		game.setConnector(this);
		
		// Inits the nethandler functions
		this.initNethandler();
	}
	
	@Override
	public Player getCorrespondinConnection(Session session) {
		return this.existingPlayers.stream().filter(i->i.getConnection()==session).findAny().get();
	}

	@Override
	public Map<Integer, Entry<Class<?>, BiConsumer<Player, Object>>> registerClientPackets() {
		return registerOfX(
			registerX(1,CPlayerInitReqeust.class,this.game::onPlayerSendInitRequest)
		);
	}

	@Override
	public Map<Class<? extends IPacketServer<Player>>, Integer> registerServerPackets() {
		return registerOf(
			register(1,SPlayerGamePlayers.class),
			register(2,SPlayerInit.class)
		);
	}

	@Override
	public void onConnect(Session session) {
		// Gets the player if he already exists
		Optional<Player> optPlayer = this.getExistingPlayer(session);

		// Gets or creates the player
		Player p = optPlayer.isPresent() ? optPlayer.get()
				: new Player(this, this.generateUnusedUUID(), this.pconfig.getRandomPlayerName(),session, this.existingPlayers.size());

		// Checks if the player got found
		if (optPlayer.isPresent()) {
			// Updates the connection
			p.setConnection(session);
			// Executes the rejoin event
			this.game.onPlayerRejoin(p);
		} else {
			// Adds the new player
			this.existingPlayers.add(p);
			// Executes the join event with the new player
			this.game.onNewPlayerJoined(p);
		}
	}

	@Override
	public void onDisconnect(Session session, int statusCode, String reason) {
		// Searches the player
		Player p = this.getExistingPlayers().stream().filter(i -> i.getConnection().equals(session)).findFirst().get();

		// Removes the session
		p.setConnection(null);

		// Executes the event
		this.game.onPlayerDisconnected(p);
	}
	
	/**
	 * Generates a new uuid that hasn't been used by any player
	 * 
	 * @return a new unique uuid
	 */
	private UUID generateUnusedUUID() {
		while (true) {
			// Generates the uuid
			UUID u = UUID.randomUUID();

			// Checks if any player already uses that id
			if (this.game.getPlayers().stream().anyMatch(i -> i.getUUID().equals(u)))
				continue;

			// The uuid is unused, use it
			return u;
		}
	}

	/**
	 * Checks if the given session corresponds to any given player
	 * 
	 * @param session
	 *            the session to look for
	 * @return if found, the player that the session corresponds to; otherwise empty
	 */
	private Optional<Player> getExistingPlayer(Session session) {
		// All cookies if there are any
		List<HttpCookie> cookies = session.getUpgradeRequest().getCookies();

		// Gets the session cookie, if there is one set
		Optional<UUID> optSessionID = cookies == null ? Optional.empty()
				: cookies.stream().filter(i -> i.getName().equalsIgnoreCase("session")).map(i -> {
					try {
						return UUID.fromString(i.getValue());
					} catch (Exception e) {
						return null;
					}
				}).filter(i -> i != null).findFirst();

		// Checks if there is no sessionid
		if (!optSessionID.isPresent())
			return Optional.empty();

		// Gets the id
		UUID sessionID = optSessionID.get();

		// Searches the user
		return this.getExistingPlayers().stream()
				// Checks if the id is equal and the player is disconnected
				.filter(i -> i.getConnection() == null && i.getUUID().equals(sessionID)).findFirst();
	}

	public List<Player> getExistingPlayers() {
		return this.existingPlayers;
	}
}
