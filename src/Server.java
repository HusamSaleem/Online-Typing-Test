import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	ServerSocket serverSocket;
	private static int port = 5014;
	
	private int proc_ID_Counter = 1;

	public static ExecutorService threadPool;

	public Server(int port, int poolSize) throws IOException {
		this.serverSocket = new ServerSocket(port);
		threadPool = Executors.newFixedThreadPool(poolSize);

	}

	public static void main(String[] args) throws IOException {
		Server server = new Server(port, 25);
		server.start();

		// Close all sockets when java program is terminated
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				server.shutDown();
			}
		});
	}

	// Starts the server and starts listening on the specified port
	// Accepts any connections
	// Makes new threads for each client to receive data from the clients
	private void start() throws IOException {
		System.out.println("Server is listening on port: " + serverSocket.getLocalPort());

		// A thread to handle the thread pool
		Thread thread = new Thread(new ThreadHandlers());
		thread.start();
		
		// A thread to handle the pinging to the clients every minute or so
		Thread thread2 = new Thread(new PingHandler());
		thread2.start();
		
		while (true) {
			// Accepts any connections
			Socket clientSocket = serverSocket.accept();
			System.out.println("Client Connected: " + clientSocket.toString());

			// Add client to existing client list and make a new thread for the client
			ClientHandler cl = new ClientHandler(clientSocket, Integer.toString(proc_ID_Counter));
			proc_ID_Counter++;

			clients.add(cl);
		}
	}

	private void shutDown() {
		System.out.println("Shutting down the server...");
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

// Makes sure that the clients are still active, if not delete them
class PingHandler implements Runnable {
	
	// Miliseconds
	private final long PING_INTERVAL = 1000;
	
	@Override
	public void run() {
		while (true) {
			for (ClientHandler client : Server.clients) {
				try {
					client.sendData("Ping!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (!client.increaseRetryCount()) {
						System.out.println("Client has been removed from active connections: " + client.S.toString());
						
						try {
							client.S.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						Server.clients.remove(client);
					}
				}
			}
			
			try {
				Thread.sleep(PING_INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class ThreadHandlers implements Runnable {

	@Override
	public void run() {

		while (true) {
			for (ClientHandler client : Server.clients) {
				Server.threadPool.execute(client);
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

class ClientHandler implements Runnable {
	public final Socket S;
	public final String PROC_ID;
	
	private int retryConnections;
	private boolean isConnected;
	private long lastPinged;

	public ClientHandler(Socket S, String PROC_ID) {
		this.S = S;
		this.PROC_ID = PROC_ID;
		this.retryConnections = 0;
		this.isConnected = true;
		this.lastPinged = System.currentTimeMillis();
	}

	@Override
	public void run() {
		try {
			processData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean sendData(String data) throws IOException {
		PrintWriter writer = new PrintWriter(S.getOutputStream());

		if (writer.checkError())
			return false;
		
		// "`" means its the end of the data line
		writer.println(data + "`");
		writer.flush();
		return true;
	}

	public String[] recieveData() throws IOException, InterruptedException {
		int red = -1;
		byte[] buffer = new byte[5 * 1024]; // A read buffer of 5 KiB
		byte[] redData;

		String clientData = "";
		String redDataText;

		// If there isn't any bytes that are ready to be read, then return a -1
		if (S.getInputStream().available() <= 0) {
			return new String[] { "-1" };
		}

		// While there is still data available
		while ((red = S.getInputStream().read(buffer)) > -1) {
			redData = new byte[red];
			System.arraycopy(buffer, 0, redData, 0, red);
			redDataText = new String(redData, "UTF-8"); // The client sends UTF-8 Encoded
			clientData += redDataText;

			if (clientData.indexOf("`") != -1) {
				break;
			}
		}

		// Turn all the sent commands into an array in case if they get combined
		String[] data = clientData.split("`");
		//clientData = clientData.replaceAll("`", "");
		return data;
	}
	
	// Reads and parses the data from the client
	public void processData() throws IOException {
		String[] data = null;
		try {
			data = recieveData();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.out.println("Something went wrong when processing data... with client: " + S.toString());
			return;
		}

		if (!data[0].equals("-1")) {
			for (String d : data) {
				System.out.println("Client: " + S.toString() + " sent this data: " + d);
				
				if (d.equals("Process_ID: " + this.PROC_ID)) {
					this.isConnected = true;
					this.lastPinged = System.currentTimeMillis();
					sendData("I see you are still alive!");
				} else if (d.equals("Process_ID Is Empty")) {
					this.lastPinged = System.currentTimeMillis();
					sendData("Process_ID" + this.PROC_ID);
				}
			}
		}
	}
	
	public boolean isAlive() {
		return this.isConnected;
	}
	
	public int getRetryCount() {
		return this.retryConnections;
	}
	
	// Returns false : Delete this client b/c its inactive
	// Returns true: its still not exceeded the retry maximum limit
	public boolean increaseRetryCount() {
		this.retryConnections++;
		
		if (this.retryConnections > 3) {
			return false;
		} else {
			return true;
		}
	}
	
	public long getLastPingTime() {
		return this.lastPinged;
	}
}