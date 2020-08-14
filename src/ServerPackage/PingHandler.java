package ServerPackage;

import java.io.IOException;
import java.util.Iterator;

//Makes sure that the clients are still active, if not delete them
public class PingHandler implements Runnable {

	// The interval for how many pings go out to clients,
	private final long PING_INTERVAL = 60000;

	@Override
	public void run() {
		while (true) {
			Iterator<ClientHandler> iter = Server.clients.iterator();

			while (iter.hasNext()) {
				ClientHandler client = iter.next();

				try {
					client.sendData("Ping!");
					Thread.sleep(500);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}

				if (System.currentTimeMillis() - client.getLastPingTime() > PING_INTERVAL) {
					client.increaseRetryCount();

					if (!client.keepAlive() || !client.isConnected()) {
						System.out.println("Client has been removed: " + client.S.toString());

						try {
							client.S.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						iter.remove();
					} else {
						client.sendConnectionInfo();
					}
				} 
			}

			System.out.println("Finished pinging clients...");

			try {
				Thread.sleep(PING_INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}