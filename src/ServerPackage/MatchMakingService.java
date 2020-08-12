package ServerPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;


public class MatchMakingService {
	LinkedHashSet<ClientHandler> playerEasyQueue2;
	public static HashMap<Integer, Game> activeGameSessions;
	
	public MatchMakingService() {
		// Linked hash set that provides uniqueness and preserves insertion order...
		this.playerEasyQueue2 = new LinkedHashSet<ClientHandler>();
		MatchMakingService.activeGameSessions = new HashMap<Integer, Game>();
	}
	
	public void addPlayerToQueue(ClientHandler c) {
		playerEasyQueue2.add(c);
		checkQueue();
	}
	
	public void checkQueue() {
		while (playerEasyQueue2.size() % 2 == 0) {
			ArrayList<ClientHandler> players = new ArrayList<ClientHandler>();
			
			Iterator<ClientHandler> i = playerEasyQueue2.iterator();
			ClientHandler c = i.next();
			
			players.add(c);
			i.remove();
			
			c = i.next();
			players.add(c);
			i.remove();
			
			Game game = new Game(players, 1);
			activeGameSessions.put(game.getGameId(), game);
		}
	}
}
