package ServerPackage;

import java.util.Iterator;
import java.util.Map.Entry;

public class GameManager implements Runnable {
	final int SLEEP_INTERVAL = 1000;
	final long SEND_DATA_INTERVAL = 5000;

	long lastSentTime = 0;

	@Override
	public void run() {
		while (true) {
			checkIfNewPlayersAreReady();
			
			if (MatchMakingService.activeGameSessions.size() > 0) {
				decreaseTimers();

				if (System.currentTimeMillis() - lastSentTime >= SEND_DATA_INTERVAL) {
					lastSentTime = System.currentTimeMillis();
					updateClientData();
				}
			}

			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void checkIfNewPlayersAreReady() {
		for (Entry<Integer, Game> g : MatchMakingService.activeGameSessions.entrySet()) {
			if (!g.getValue().isGameStarted() && g.getValue().playersAreReady()) {
				g.getValue().notifyClientsGameStarted();
			}
		}
	}

	public void decreaseTimers() {
		Iterator<Entry<Integer, Game>> iter = MatchMakingService.activeGameSessions.entrySet().iterator();

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

	public void updateClientData() {
		for (Entry<Integer, Game> g : MatchMakingService.activeGameSessions.entrySet()) {
			if (g.getValue().isGameStarted())
				g.getValue().updateClientData();
		}
	}
}