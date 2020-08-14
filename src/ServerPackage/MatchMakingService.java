package ServerPackage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class MatchMakingService {
	// 2 Player Matchmaking Queues
	public Queue<ClientHandler> playerEasyQueue2;
	public Queue<ClientHandler> playerChallengingQueue2;
	public Queue<ClientHandler> playerInsaneQueue2;

	// All Active game sessions
	public static HashMap<Integer, Game> activeGameSessions;

	public MatchMakingService() {
		this.playerEasyQueue2 = new ArrayDeque<ClientHandler>();
		this.playerChallengingQueue2 = new ArrayDeque<ClientHandler>();
		this.playerInsaneQueue2 = new ArrayDeque<ClientHandler>();

		MatchMakingService.activeGameSessions = new HashMap<Integer, Game>();
	}

	public void addPlayerToQueue(ClientHandler c, int difficulty) {
		if (difficulty == 1)
			playerEasyQueue2.add(c);
		else if (difficulty == 2)
			playerChallengingQueue2.add(c);
		else if (difficulty == 3)
			playerInsaneQueue2.add(c);
	}

	public void checkQueues(Queue<ClientHandler> queue, int difficulty) {
		while (queue.size() % 2 == 0) {
			ArrayList<ClientHandler> players = new ArrayList<ClientHandler>();

			players.add(queue.poll());
			if (players.get(0) == null)
				return;

			players.add(queue.poll());
			if (players.get(1) == null)
				return;

			if (players.get(0).PROC_ID == players.get(1).PROC_ID) {
				System.out.println("These two players have the same Process Id somehow: {" + players.get(0).getName()
						+ ", " + players.get(1).getName() + "}");
				return;
			}

			int alivePlayers = checkIfPlayersAreAlive(players);

			if (alivePlayers == -1) {
				Game game = new Game(players, difficulty);
				activeGameSessions.put(game.getGameId(), game);
			} else {
				if (alivePlayers == 0) {
					System.out.println(
							"Removed player from the Queue for inactivity: " + players.get(alivePlayers + 1).getName());
					queue.add(players.get(alivePlayers + 1));
				} else if (alivePlayers == 1) {
					System.out.println(
							"Removed player from the Queue for inactivity: " + players.get(alivePlayers - 1).getName());
					queue.add(players.get(alivePlayers - 1));
				}
			}
		}
	}

	public int checkIfPlayersAreAlive(ArrayList<ClientHandler> players) {
		final int DATA_THRESHOLD = 2000;

		for (int i = 0; i < players.size(); i++) {
			try {
				players.get(i).sendData("Are you alive?");
				
				try {
					Thread.sleep(DATA_THRESHOLD + 500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (System.currentTimeMillis() - players.get(i).getLastPingTime() > DATA_THRESHOLD * 2 + 500) {
					return i;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}
}