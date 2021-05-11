package de.projektlabor.makiekillerbot.cac;

import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;

import de.projektlabor.makiekillerbot.cac.achievements.AchievementManager;
import de.projektlabor.makiekillerbot.cac.config.Config;
import de.projektlabor.makiekillerbot.cac.config.ConfigLoadException;
import de.projektlabor.makiekillerbot.cac.game.Game;
import de.projektlabor.makiekillerbot.cac.game.logic.Gameloop;
import de.projektlabor.makiekillerbot.cac.game.pi.NethandlerPi;
import de.projektlabor.makiekillerbot.cac.game.player.NethandlerPlayer;
import de.projektlabor.makiekillerbot.cac.game.player.PlayerConfig;
import spark.Route;

public class Start {

	// TODO: Outsource static time variable. To config file or smth. Maybe together with the name and color options
	
	// Amount of time a user should have left if a new user just joined and is read to take over
	public static final long ADDITIONAL_CONTROL_TIME_ON_EXIT = 1000*10;
	
	// Max amount of the time the user is allowd to controll the pi
	public static final long CONTROL_TIME = 1000*60*5;
	
	// How long it takes to remove a disconnected player that hasn't rejoined
	public static final long MAX_DISCONNECT_TIME = 10*1000;
	
	// Default error handler for most 404 pages
	public static Route ROUTE_NOT_FOUND_HANDLER = (req,res)->{
		res.status(404);
		return "Marten ist schuld 404";
	};
	
	public static void main(String[] args) {
		// Creates the config
		Config cfg = new Config(new File("config.json"));
		
		// Creates the achievement-manager
		AchievementManager avtm = new AchievementManager();
		PlayerConfig pcfg = new PlayerConfig();
		
		// Registers the different subconfigs
		cfg.registerSubconfig("achievements", avtm);
		cfg.registerSubconfig("players", pcfg);
		
		try {
			// Loads the config
			cfg.load();
		}catch(ConfigLoadException e) {
			System.err.println(String.format("Failed to load config. Subconfig '%s' could not be loaded: %s", e.getSectionName(),e.getSubException().getMessage()));
			System.exit(-1);
			return;
		} catch(IOException e) {
			System.err.println("Failed to load config: (i/o: read/write): "+e.getMessage());
			System.err.println("Maybe there are some problems with the read/write permissions or the file has been opend in another process?");
			System.exit(-1);
			return;
		} catch(JSONException e) {
			System.err.println("Failed to load config. Could not parse json inside the config file. Maybe delete the config and implement the old one into the newly generated one.");
			System.exit(-1);
			return;
		}
		
		// Creates the game-object
		Game game = new Game(cfg,avtm);
		
		// Settings for the webserver
		port(80);

		// Registers the static webpage for the players
		staticFiles.location("/webpage");

		// Registers the websockets
		webSocket("/ws/connect", new NethandlerPlayer(game,pcfg));
		webSocket("/ws/pi",new NethandlerPi(game));

		// Registers the websocket-coverhandlers
		get("/ws/connect",(a,b)->null);
		get("/ws/pi",(a,b)->null);
		
		// Finishes the achievement-manager init
		avtm.finishInit("/achievements");
		
		// Registers the 404-handler
		get("*", ROUTE_NOT_FOUND_HANDLER);
		
		// Starts the webserver
		init();
		
		// Waits for the start
		awaitInitialization();
		
		// Creates a simple game-loop thread
		new Thread(new Gameloop(game)).start();
    }
}
