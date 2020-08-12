package ServerPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class MatchMakingService {
	Queue<ClientHandler> playerEasyQueue2;
	public static HashMap<Integer, Game> activeGameSessions;
	
	public MatchMakingService() {
		this.playerEasyQueue2 = new LinkedList<ClientHandler>();
	}
	
	public void addPlayerToQueue(ClientHandler c) {
		playerEasyQueue2.add(c);
		checkQueue();
	}
	
	public void checkQueue() {
		while (playerEasyQueue2.size() % 2 == 0) {
			ArrayList<ClientHandler> players = new ArrayList<ClientHandler>();
			players.add(playerEasyQueue2.poll());
			players.add(playerEasyQueue2.poll());
			Game game = new Game(players, 1);
			activeGameSessions.put(game.getGameId(), game);
		}
	}
}
