package de.projektlabor.makiekillerbot.cac.connection.packets;

import org.json.JSONObject;

public interface IPacketServer<Type>{
	
	/**
	 * Writes all packet-values to the packet
	 */
	public void writePacketData(JSONObject packet);
}
