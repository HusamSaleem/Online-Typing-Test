package ServerPackage;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * <p><b> Manages all the active and ready game sessions </b></p>
 * @author Husam Saleem
 */
public class GameManager implements Runnable {
	final int SLEEP_INTERVAL = 1000;
	final long SEND_DATA_INTERVAL = 5000;

	long lastSentTime = 0;

	@Override
	public void run() {
		while (true) {
			if (Server.mmService.activeGameSessions.size() > 0) {
				decreaseTimers();

				if (System.currentTimeMillis() - lastSentTime >= SEND_DATA_INTERVAL) {
					lastSentTime = System.currentTimeMillis();
					updateClientData();
				}
			}
			
			checkIfNewPlayersAreReady();
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p><b> Checks to see if the players are ready to play </b></p>
	 */
	public void checkIfNewPlayersAreReady() {
		for (Entry<Integer, Game> g : Server.mmService.activeGameSessions.entrySet()) {
			if (!g.getValue().isGameStarted() && g.getValue().playersAreReady()) {
				g.getValue().notifyClientsGameStarted();
			}
		}
	}

	/**
	 * <p><b> Decrease the timer of each active game session </b></p>
	 */
	public void decreaseTimers() {
		Iterator<Entry<Integer, Game>> iter = Server.mmService.activeGameSessions.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<Integer, Game> g = iter.next();

			if (g.getValue().isGameStarted()) {
				g.getValue().decreaseTimer();

				if (g.getValue().isGameDone()) {
					System.out.println("Game session has been completed and removed from active games, ID: "
							+ g.getValue().getGameId());
					iter.remove();
				}
			}
		}
	}

	/**
	 * <p><b> Sends updated information to the clients </b></p>
	 */
	public void updateClientData() {
		for (Entry<Integer, Game> g : Server.mmService.activeGameSessions.entrySet()) {
			if (g.getValue().isGameStarted())
				g.getValue().updateClientData();
		}
	}
}