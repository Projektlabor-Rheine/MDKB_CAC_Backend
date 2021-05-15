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
	
	public Controller(GameConfig cfg) {
		this.cfg=cfg;
	}
	
	public void nextController(Player player) {
		this.player = player;
		this.controllerSince = System.currentTimeMillis();
		this.controllerUntil = System.currentTimeMillis() + this.cfg.getControllingTime();
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
