package ServerPackage;

import java.util.Map.Entry;

public class GameManager implements Runnable {
	final int SLEEP_INTERVAL = 1000;

	// 5 Seconds
	final long SEND_DATA_INTERVAL = 5;

	long lastSentTime = 0;
	
	@Override
	public void run() {
		while (true) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void decreaseTimers() {
		for (Entry<Integer, Game> g : MatchMakingService.activeGameSessions.entrySet()) {

			if (g.getValue().gameStarted) {
				g.getValue().decreaseTimer();

				if (g.getValue().isGameDone()) {
					System.out.println("Game session has been completed and removed from active games: "
							+ g.getValue().getGameId());
					MatchMakingService.activeGameSessions.remove(g.getKey());
				}

			}
		}
	}

	public void updateClientData() {
		for (Entry<Integer, Game> g : MatchMakingService.activeGameSessions.entrySet()) {
			if (g.getValue().gameStarted)
				g.getValue().updateClientData();
		}
	}
	

}
