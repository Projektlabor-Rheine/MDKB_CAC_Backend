package de.projektlabor.makiekillerbot.cac.game.logic;

import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.util.Timer;

/**
 * Gameloop thread that runs constantly to provide a loop for the game
 * @author Noah
 *
 */
public class Gameloop implements Runnable{

	private final long
	CPU_DELAY = 500, 			// Delay for the cpu
	CLEANUP_LOOP = 1000*10		// Delay for the cleanup loop
	;	
	
	
	
	// Timer for the clearup loop
	private Timer cleanupTimer = new Timer();
	
	// Reference to the game
	private Game game;
	
	public Gameloop(Game game) {
		this.game = game;
	}
	
	@Override
	public void run() {
		// Runs consistently
		while(true) {
			try {
				// Delay for the cpu
				Thread.sleep(this.CPU_DELAY);
			} catch (InterruptedException e) {}
			
			// Updates the cleanup timer
			if(this.cleanupTimer.hasReachedIfReset(this.CLEANUP_LOOP))
				// Executes the cleanup event
				this.game.onCleanup();
			
			// Executes the gameloop tick
			this.game.onGameloopTick();
		}
	}	
	
}
