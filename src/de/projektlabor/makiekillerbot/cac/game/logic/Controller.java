package de.projektlabor.makiekillerbot.cac.game.logic;

import de.projektlabor.makiekillerbot.cac.game.GameConfig;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

public class Controller {

	// Config
	private final GameConfig cfg;
	
	// The current controlling player
	// Can be null if no player is the current controller
	private Player player;
	
	// Since when the current controller is active
	private long controllerSince;

	// Current expectation until the player will be the controller
	private long controllerUntil;
	
	// Event listener for the controller update
	private Runnable onUpdate;
	
	public Controller(GameConfig cfg,Runnable onUpdate) {
		this.cfg=cfg;
		this.onUpdate=onUpdate;
	}
	
	/**
	 * Updates the controller and inserts the next player.
	 * @param player the next controller (can be null)
	 */
	public void nextController(Player player) {
		// TODO
		System.out.println("New controller "+player);
		this.player = player;
		this.controllerSince = System.currentTimeMillis();
		this.controllerUntil = System.currentTimeMillis() + this.cfg.getControllingTime();
		// Executes the event
		this.onUpdate.run();
		// TODO: Remove
		System.out.println("Until: "+this.controllerUntil);
	}
	
	public long getControllerUntil() {
		return controllerUntil;
	}
	
	public void setControllerUntil(long controllerUntil) {
		this.controllerUntil = controllerUntil;
	}
	
	public void greantAdditionalTime(long time) {
		this.controllerUntil+=time;
	}
	
	public boolean isPlayerController(Player p) {
		return this.player == p;
	}
	
	public boolean doesControllerExists() {
		return this.player != null;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public long getControllerSinceTime() {
		return this.controllerSince;
	}
}
