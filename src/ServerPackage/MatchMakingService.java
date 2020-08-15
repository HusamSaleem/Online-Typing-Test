package ServerPackage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

/**
 * <p><b> This class will handle the Matchmaking service of the game </b></p>
 * @author Husam Saleem
 */
public class MatchMakingService {
	// 2 Player Matchmaking Queues
	public Queue<ClientHandler> playerEasyQueue2;
	public Queue<ClientHandler> playerChallengingQueue2;
	public Queue<ClientHandler> playerInsaneQueue2;

	// All Active game sessions
	public HashMap<Integer, Game> activeGameSessions;

	public MatchMakingService() {
		this.playerEasyQueue2 = new ArrayDeque<ClientHandler>();
		this.playerChallengingQueue2 = new ArrayDeque<ClientHandler>();
		this.playerInsaneQueue2 = new ArrayDeque<ClientHandler>();

		this.activeGameSessions = new HashMap<Integer, Game>();
	}

	/**
	 * <p><b> Adds a player to the respective Queue </b></p>
	 * @param c
	 * @param difficulty
	 */
	public void addPlayerToQueue(ClientHandler c, int difficulty) {
		if (difficulty == 1)
			playerEasyQueue2.add(c);
		else if (difficulty == 2)
			playerChallengingQueue2.add(c);
		else if (difficulty == 3)
			playerInsaneQueue2.add(c);
	}

	/**
	 * <p><b> Checks to see if any Queue has enough players to start the game session </b></p>
	 * <p><b> Also checks to see if the players in Queue are still active, if not they get removed </b></p>
	 * @param queue
	 * @param difficulty
	 */
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

			int inactivePlayerIndex = checkIfPlayersAreAlive(players);

			if (inactivePlayerIndex == -1) {
				Game game = new Game(players, difficulty);
				activeGameSessions.put(game.getGameId(), game);
			} else {
				if (inactivePlayerIndex == 0) {
					//System.out.println("Removed player from the Queue for inactivity: " + players.get(inactivePlayerIndex + 1).getName());
					queue.add(players.get(inactivePlayerIndex + 1));
				} else if (inactivePlayerIndex == 1) {
					//System.out.println("Removed player from the Queue for inactivity: " + players.get(inactivePlayerIndex - 1).getName());
					queue.add(players.get(inactivePlayerIndex - 1));
				}
			}
		}
	}

	/**
	 * <p><b> Pings players and expects a message back otherwise they are not ready... </b></p>
	 * @param players
	 * @return
	 */
	public int checkIfPlayersAreAlive(ArrayList<ClientHandler> players) {
		final int DATA_THRESHOLD = 2000;

		for (int i = 0; i < players.size(); i++) {
			if (!players.get(i).isConnected())
				return i;

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
				System.out.println(e.getMessage());
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p><b> Removes a player from any Queue that they are in </b></p>
	 * @param player
	 */
	public void removePlayerFromQueue(ClientHandler player) {
		playerEasyQueue2.remove(player);
		playerChallengingQueue2.remove(player);
		playerInsaneQueue2.remove(player);
	}
}