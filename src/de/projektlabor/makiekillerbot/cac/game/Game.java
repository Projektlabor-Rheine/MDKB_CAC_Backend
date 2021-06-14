package de.projektlabor.makiekillerbot.cac.game;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.projektlabor.makiekillerbot.cac.achievements.Achievement;
import de.projektlabor.makiekillerbot.cac.achievements.AchievementManager;
import de.projektlabor.makiekillerbot.cac.config.Config;
import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.logic.Controller;
import de.projektlabor.makiekillerbot.cac.game.pi.RaspberryPi;
import de.projektlabor.makiekillerbot.cac.game.pi.packets.server.SPiControllsupdate;
import de.projektlabor.makiekillerbot.cac.game.player.NethandlerPlayer;
import de.projektlabor.makiekillerbot.cac.game.player.Player;
import de.projektlabor.makiekillerbot.cac.game.player.packets.PacketUpdater;
import de.projektlabor.makiekillerbot.cac.game.player.packets.client.CPlayerControllsupdate;
import de.projektlabor.makiekillerbot.cac.game.player.packets.client.CPlayerInitReqeust;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerGameAchievements;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerGameController;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerGamePlayers;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerGameRaspiStatus;
import de.projektlabor.makiekillerbot.cac.game.player.packets.server.SPlayerInit;

/**
 * Provides all basic functions of the game. Holds all game objects
 * 
 * @author Noah
 *
 */

public class Game {

	// The raspberry pi that controlls the bot and is used for the communication
	private RaspberryPi raspberrypi;

	// The controller of the pi
	private Controller controller;

	
	
	// If there is currently no other controller and onjoin additional 10
	// leave-seconds should be added until the next one takes over.
	private boolean performJoinAction = false;

	// Packet-Updater for the list of players
	private PacketUpdater puPlayer;
	// Packet-updater for the current controlling player
	private PacketUpdater puController;
	// Packet-updater for the already found achievements
	private PacketUpdater puAchievements;
	// Packet-updater for the raspi-connection status
	private PacketUpdater puRaspiConnection;

	// The connector for all players
	private NethandlerPlayer nethandler;

	// The manager for all achievements
	private AchievementManager avmtManager;
	
	// Game-config
	private GameConfig gameConfig;
	
	// Reference to the config
	private Config config;

	public Game(Config config,GameConfig gameConfig,AchievementManager avmtManager) {
		this.config=config;
		this.gameConfig=gameConfig;
		this.avmtManager = avmtManager;
		this.controller = new Controller(gameConfig,this::sendRaspiStopMovement);
		avmtManager.setGameReference(this);
		this.initUpdaters();
	}

	/**
	 * Creates all packet-updaters
	 */
	private void initUpdaters() {
		// The player-packet updater that sends all current player-states
		this.puPlayer = new PacketUpdater(this, () -> new SPlayerGamePlayers(this.getPlayers()), 10);

		// The controller-packet updater that sends the currently controlling player
		this.puController = new PacketUpdater(this, () -> {
			// Checks if there is currently no controller
			if (!this.controller.doesControllerExists())
				return null;

			// Returns the update-packet
			return new SPlayerGameController(this.controller);
		}, 10);

		// The achievement-packet updater
		this.puAchievements = new PacketUpdater(this,
				() -> new SPlayerGameAchievements(this.getAchievementManager().getLoadedAchievements()), 10);

		// The raspi-connection-packet updater
		this.puRaspiConnection = new PacketUpdater(this,
				() -> new SPlayerGameRaspiStatus(this.getRaspberrypi().getConnection() != null), 10);
	}

	
	
	/**
	 * Packet receive event for the initrequest packet
	 */
	public void onPlayerSendInitRequest(Player p, CPlayerInitReqeust re) {
		// Sends the update-packet to the player
		p.sendPacket(new SPlayerInit(this, p));
	}

	/**
	 * Executes when a player get's removed from the game (He can no longer rejoin)
	 * 
	 * @param p
	 *            the player
	 */
	public void onPlayerRemoved(Player... players) {

		// TODO: Remove
		if(players.length > 0)
			System.out.println("Removed players: "+Arrays.toString(players));
		
		// Gets the controller that has been removed; empty if the controller hasn't been removed
		Optional<Player> ctrl = Arrays.stream(players).filter(this.controller::isPlayerController).findAny();
		
		// Checks if the controller got removed
		if (ctrl.isPresent()) {
			// Searches the next controller
			Optional<Player> nxt = this.searchNextConnectedPlayer(ctrl.get());

			// Sets the next controller either to a valid one or null
			this.nextController(nxt.isPresent() ? nxt.get() : null);

			// Orders the queue
			this.updateQueueIndexes();

			// Sends the update-packets
			this.puPlayer.sendAndReset();
			this.puController.sendAndReset();
		} else {
			// Orders the queue
			this.updateQueueIndexes();
			// Send the update to all players
			this.puPlayer.sendAndReset();
		}
	}

	/**
	 * Executes when a player get's disconnected
	 * 
	 * @param p
	 *            the player
	 */
	public void onPlayerDisconnected(Player p) {

		// TODO: Remove
		System.out.println("Disconnected player "+p);
		
		// Sets the player connection to disconnected
		p.setConnection(null);
		// Sends the update to all players
		this.puPlayer.sendAndReset(p);
		
		// Checks if the controller got disconnected
		if(this.controller.isPlayerController(p))
			this.sendRaspiStopMovement();
	}

	/**
	 * Executes when players join (or rejoin)
	 * 
	 * @param players
	 *            all players that joined or rejoined
	 */
	public void onPlayersJoined(Player... players) {

		System.out.println("Joined players "+Arrays.toString(players));

		// If there is a new controller
		boolean hasNewControllerFlag = false;
		
		// Iterates over all joined players
		for(Player p : players) {			
			// Checks if no player is currently in control
			if (!this.controller.doesControllerExists()) {
				// Sets the connected player as the next controller
				this.nextController(p);
				
				// Update the ctrl-flag
				hasNewControllerFlag = true;
			}
			// Checks if a previous controller has already outrun his time
			else if (this.performJoinAction) {
				this.performJoinAction = false;
				this.controller.setControllerUntil(System.currentTimeMillis() + this.gameConfig.getControllerTimeWhenNewJoin());

				// Update the ctrl-flag
				hasNewControllerFlag = true;
			}
			
			// Sends the init-packet to the player
			p.sendPacket(new SPlayerInit(this, p));
		}
		
		// Sends the player-update packet to all players except the newly joined ones
		this.puPlayer.sendAndReset(players);
		
		// Checks if there is a new controller
		if(hasNewControllerFlag)
			// Sends the new-controller packet to all except the new-joined ones
			this.puController.sendAndReset(players);
		
	}

	/**
	 * Packet receive event for the update key event from the players
	 */
	public void onPlayerSendKeypress(Player p,CPlayerControllsupdate pkt) {

		// TODO: Remove
		System.out.println(p+" send keyupdate: "+pkt);
		
		// Checks if the player is the controller
		if(!this.controller.isPlayerController(p))
			return;
		
		// Checks if the pi is disconnected
		if(!this.raspberrypi.isConnected())
			return;
		
		// Sends the packet
		this.raspberrypi.sendPacket(new SPiControllsupdate(pkt));
	}
	
	
	
	/**
	 * Executes when the raspi gets connected (or reconnected)
	 * @param lastConnectionTime how long the pi has been disconnected
	 */
	public void onRaspiConnect(long lastConnectionTime) {
		
		// TODO: Remove
		System.out.println("New rpi con");
		
		// Checks if there is currently a player controlling the pi
		if (this.controller.doesControllerExists()) {
			
			// Calculates the time that the pi has been disconnected relative to the
			// controller time
			long disconnectedTime = System.currentTimeMillis()
					- Math.max(this.controller.getControllerSinceTime(), lastConnectionTime);

			// Appends the calculated time
			this.controller.greantAdditionalTime(disconnectedTime);

			// Sends the packet
			this.puController.sendAndReset();
		}

		// Sends the packet
		this.puRaspiConnection.sendAndReset();
	}

	/**
	 * Executes when the raspi gets disconnected
	 */
	public void onRaspiDisconnect() {

		// TODO: Remove
		System.out.println("Rpi left");
		
		// Resets the join-action
		this.performJoinAction = false;

		// Sends the update to all players
		this.puController.sendAndReset();
	}

	
	
	/**
	 * Executes when a new achievement get's found
	 * 
	 * @param achievement
	 *            the achievement that got found
	 */
	public void onAchievementFound(Achievement achievement) {
		// Sends the update to all players
		this.puAchievements.sendAndReset();
	}

	/**
	 * Main game loop that can be use for repetitive tasks Executes every half a
	 * second or so
	 */
	public void onGameloopTick() {

		// If the joinaction hasn't been set, the controller exists, the raspi is
		// connected and the controller has outrun his time
		boolean searchNext = !this.performJoinAction && this.controller.doesControllerExists()
				&& this.getRaspberrypi().isConnected()
				&& System.currentTimeMillis() >= this.controller.getControllerUntil();

		if (searchNext) {
			// Searches a new controller
			Optional<Player> nxtCtrl = this.searchNextConnectedPlayer(this.controller.getPlayer());

			// Checks if a valid new controller could be found
			if (nxtCtrl.isPresent()) {
				// Sets the next controller
				this.nextController(nxtCtrl.get());

				// Sends the update
				this.puPlayer.sendAndReset();
				this.puController.sendAndReset();
			} else
				// Grants the controller the additional time and sets the flag to set the last
				// 10 seconds on join of a new player
				this.performJoinAction = true;
		}

		// Updates all update-packets
		this.puPlayer.onTick();
		this.puController.onTick();
		this.puAchievements.onTick();
		this.puRaspiConnection.onTick();
		
		// Update the nethandler
		this.nethandler.onTick();

	}

	/**
	 * Cleanup-loop that executes every once in a while to cleanup any gameobjects
	 * such as disconnected players
	 */
	public void onCleanup() {
		// Collects all timeouted players
		Player[] timeouted = this.getPlayers().stream()
			// Filters all player that have been disconnected for more than 30 seconds
			.filter(p -> p.getConnection() == null && p.getConnectionTimer().hasReached(this.gameConfig.getRejoinTime()))
			// Collects them
			.toArray(Player[]::new);
		
		// Removes them from the list
		for(Player p : timeouted)
			this.getPlayers().remove(p);
		
		// Executes the on removed event
		this.onPlayerRemoved(timeouted);
	}

	
	/**
	 * Sets all players back to a valid position (So no gab opens in the queue) and
	 * sets the next controller if the current controller is null
	 */
	private void updateQueueIndexes() {
		// Updates the queue index
		for (int i = 0; i < this.getPlayers().size(); i++)
			// Updates the queue-index
			this.getPlayers().get(i).setQueueIndex(i);
	}

	/**
	 * Searches the next player in the queue that is connected
	 * 
	 * @param skip
	 *            this player will be skipped
	 * @return empty if no other connected player could be found; otherwise the next
	 *         connected player in the queue
	 */
	private Optional<Player> searchNextConnectedPlayer(Player skip) {
		return this.getPlayers().stream().filter(i -> i.getConnection() != null).filter(i -> !i.equals(skip))
				.findFirst();
	}

	/**
	 * Sets the given player as the next controller
	 * 
	 * @param p
	 *            - the next controller. Nullable if no new controller could be
	 *            found
	 */
	private void nextController(Player p) {

		// Moves the controller to the back of the list if he exists
		if (this.controller.doesControllerExists() && this.getPlayers().contains(this.controller.getPlayer())) {
			this.getPlayers().remove(this.controller.getPlayer());
			this.getPlayers().add(this.controller.getPlayer());
		}

		// Moves the new controller to the top of the list if he exists
		if (p != null && this.getPlayers().contains(p)) {
			this.getPlayers().remove(p);
			this.getPlayers().add(0, p);
		}

		// Orders the queue indexes
		this.updateQueueIndexes();

		// Sets the next controller
		this.controller.nextController(p);
		// Reset
		this.performJoinAction = false;
	}

	/**
	 * Broadcasts the given packet to all players but the given players
	 * 
	 * @param packet
	 *            the packet that shall be send
	 * @param players
	 *            all players where the packet should not be send to
	 */
	public void broadcastPacketExcept(IPacketServer<Player> packet, Player... players) {
		// Prepares the packet and converts it to the raw bytes
		String finPkt = this.nethandler.getFinalizedPacket(packet).toString();

		// Sends the packet to all players that are connected
		main: for (Player p : this.getPlayers()) {
			// Checks if the player isn't connected
			if (p.getConnection() == null)
				return;

			// Checks if the packet should not be send to this player
			for (Player p2 : players)
				if (p2.equals(p))
					continue main;

			// Sends the raw packet-data
			p.sendRawPacket(finPkt);
		}
	}

	/**
	 * Broadcasts the given packet to all players
	 * 
	 * @param packet
	 *            the packet to broadcast
	 */
	public void broadcastPacket(IPacketServer<Player> packet) {
		this.broadcastPacketExcept(packet);
	}

	public AchievementManager getAchievementManager() {
		return this.avmtManager;
	}

	public List<Player> getPlayers() {
		return this.nethandler.getExistingPlayers();
	}

	public RaspberryPi getRaspberrypi() {
		return this.raspberrypi;
	}

	public void setConnector(NethandlerPlayer connector) {
		this.nethandler = connector;
	}
	public void setRaspberrypi(RaspberryPi raspberrypi) {
		this.raspberrypi = raspberrypi;
	}
	
	/**
	 * Sends a packet to the pi that lets him stop moving
	 */
	public void sendRaspiStopMovement() {
		// Checks if the rpi is not connected
		if(!this.raspberrypi.isConnected())
			return;
		
		// Sends the packet
		this.raspberrypi.sendPacket(new SPiControllsupdate());
	}
	
	public Controller getController() {
		return this.controller;
	}
	
	public Config getConfig() {
		return this.config;
	}
}
