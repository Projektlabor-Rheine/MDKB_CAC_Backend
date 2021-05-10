package de.projektlabor.makiekillerbot.cac.game.player.packets;

import java.util.function.Supplier;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketServer;
import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.game.player.Player;
import de.projektlabor.makiekillerbot.cac.util.Timer;

public class PacketUpdater {

	// Reference to the game
	private Game game;
	
	// The function that can return the required packet
	private Supplier<IPacketServer<Player>> onSupplyPacket;
	
	// How long to wait between updates
	private long updateTime;
	
	// Timer
	private Timer timer = new Timer();
	
	/**
	 * @param game reference to the main game
	 * @param onSupplyPacket the method to gain the newest state of the game that shall be updated
	 * @param updateTime how many time (in seconds) it takes until a new update should be send
	 */
	public PacketUpdater(Game game,Supplier<IPacketServer<Player>> onSupplyPacket,long updateTime) {
		this.game=game;
		this.onSupplyPacket=onSupplyPacket;
		this.updateTime = updateTime*1000;
	}
	
	/**
	 * Executes every few ticks to check if the current update needs to be send
	 */
	public void onTick() {
		
		// Checks if the timer is not ready
		if(!this.timer.hasReached(this.updateTime))
			return;
		
		// Sends and resets the phase
		this.sendAndReset();
	}
	
	/**
	 * Sends the next packet to all players and resets the timer
	 * @param optSkip all specified players will be skipped
	 */
	public void sendAndReset(Player... optSkip) {
		// Resets the timer
		this.timer.reset();
		
		// Gets the packet
		IPacketServer<Player> packet = this.onSupplyPacket.get();
		
		// Checks if the packet is not available
		if(packet == null)
			return;
		
		// Sends the packet
		this.game.broadcastPacketExcept(packet, optSkip);
	}
}
