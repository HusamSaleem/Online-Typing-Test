package ServerPackage;

//Listens for data from each client and processes it 
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
