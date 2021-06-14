package de.projektlabor.makiekillerbot.cac.game;

import de.projektlabor.makiekillerbot.cac.config.SubConfig;
import de.projektlabor.makiekillerbot.cac.config.loading.Loadable;
import de.projektlabor.makiekillerbot.cac.config.loading.loaders.IntLoader;
import de.projektlabor.makiekillerbot.cac.config.loading.loaders.Longloader;
import de.projektlabor.makiekillerbot.cac.config.loading.loaders.Stringloader;
import spark.Request;
import spark.Response;

public class GameConfig extends SubConfig {

	// Amount of time a controller additionally should have left if a new user just
	// joined and is read to take over. Will only be used if the controller was
	// already driving over their time
	@Loadable(name="controller_time_when_new_join",loader=Longloader.class)
	private long controllerTimeWhenNewJoin;

	// Max amount of the time the user is allowed to control the pi
	@Loadable(name="controlling_time",loader=Longloader.class)
	private long controllingTime;

	// How long it takes to remove a disconnected player that hasn't rejoined
	@Loadable(name="rejoin_time",loader=Longloader.class)
	private long rejoinTime;

	// The text that get's send when a 404-handler gets requested
	@Loadable(name="404_page_text",loader=Stringloader.class)
	private String notFoundText;
	
	@Loadable(name="port", loader=IntLoader.class)
	private int port;
	
	public GameConfig() {
		this.enableReflectedConfig();
	}
	
	@Override
	public void loadExampleConfig() {
		this.controllerTimeWhenNewJoin = 1000 * 10;
		this.controllingTime = 1000 * 60 * 5;
		this.rejoinTime = 10 * 1000;
		this.notFoundText = "Marten ist schuld";
		this.port = 80;
	}
	
	
	/**
	 * 404-page handler
	 */
	public Object routeNotFoundHandler(Request request, Response response) throws Exception{
		response.status(404);
		return this.notFoundText;
	}
	
	
	public long getControllerTimeWhenNewJoin() {
		return this.controllerTimeWhenNewJoin;
	}
	public long getControllingTime() {
		return this.controllingTime;
	}
	public String getNotFoundText() {
		return this.notFoundText;
	}
	public long getRejoinTime() {
		return this.rejoinTime;
	}
	public int getPort() {
		return this.port;
	}
}
