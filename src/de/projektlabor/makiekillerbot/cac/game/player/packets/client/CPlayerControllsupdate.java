package de.projektlabor.makiekillerbot.cac.game.player.packets.client;

import org.json.JSONObject;

import de.projektlabor.makiekillerbot.cac.connection.packets.IPacketClient;
import de.projektlabor.makiekillerbot.cac.game.player.Player;

/**
 * Packet by the controller that updates which keys are pressed
 * @author Noah
 *
 */

public class CPlayerControllsupdate implements IPacketClient<Player>{

	// The string-codes in the order that they will be read into the pressed keys array
	public static final String[] KEYS = {"w","a","s","d","up","down","left","right"};

	// The indexes of the key-state on the pressed array
	// eg. pressedKeys[KEY_W] would return if the w-key is pressed
	public static final int
	KEY_W 		= 0,
	KEY_A 		= 1,
	KEY_S 		= 2,
	KEY_D 		= 3,
	KEY_UP 		= 4,
	KEY_DOWN 	= 5,
	KEY_LEFT 	= 6,
	KEY_RIGHT 	= 7;
	
	
	// The pressed status of all keys. Use the above indexes to get a key
	public boolean[] pressedKeys = new boolean[KEYS.length];
	
	public CPlayerControllsupdate() {}
	
	@Override
	public void readPacketData(JSONObject packet) throws Exception {
		// Iterates over all known-keys
		for(byte i=0;i<KEYS.length;i++)
			// Gets the pressed-status and insert it into the array
			this.pressedKeys[i] = packet.getBoolean(KEYS[i]);
	}
}
