package de.projektlabor.makiekillerbot.cac.connection.packets;

import org.json.JSONObject;

public interface IPacketClient<Type>{

	/**
	 * Reads all packet values from the given json-object
	 */
	public void readPacketData(JSONObject packet) throws Exception;
	
}
