package ServerPackage;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p><b> Pings all clients to make sure that there are no sockets opened for no reason </b></p>
 * @author Husam Saleem
 */
public class PingHandler implements Runnable {

	// The interval for when pings go out to clients,
	private final long PING_INTERVAL = 60000;

	@Override
	public void run() {
		while (true) {
			Iterator<ClientHandler> iter = Server.clients.iterator();

			while (iter.hasNext()) {
				ClientHandler client = iter.next();

				// If the client is not connected anymore - >close the connection
				if (!client.isConnected()) {
					System.out.println("Client has been removed: " + client.S.toString());
					Server.mmService.removePlayerFromQueue(client);
					try {
						client.S.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					iter.remove();
					continue;
				}

				try {
					client.sendData("Ping!");
					Thread.sleep(500);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}

				// If the client exceeded the maximum amouunt of times they could be pinged with no response -> close the connection 
				if (System.currentTimeMillis() - client.getLastPingTime() > PING_INTERVAL) {
					client.increaseRetryCount();

					if (!client.keepAlive() || !client.isConnected()) {
						System.out.println("Client has been removed: " + client.S.toString());
						Server.mmService.removePlayerFromQueue(client);
						try {
							client.S.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						iter.remove();
					}
				} else {
					client.sendConnectionInfo();
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