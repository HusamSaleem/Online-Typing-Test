package ServerPackage;

/**
 * <p><b> Listens for each client's incoming data and executes them in a threadpool </b></p>
 * @author Husam Saleem
 */
public class ThreadHandlers implements Runnable {

	final int DATA_LISTEN_INTERVAL = 1000;
	@Override
	public void run() {

		while (true) {
			for (ClientHandler client : Server.clients) {
				Server.threadPool.execute(client);
			}
			
			try {
				Thread.sleep(DATA_LISTEN_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
