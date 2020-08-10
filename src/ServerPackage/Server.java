package ServerPackage;
import java.io.IOException;
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

		// A thread to handle the thread pool and the client's tasks
		Thread thread = new Thread(new ThreadHandlers());
		thread.start();
		
		// A thread to handle the pinging to the clients every minute or so
		Thread thread2 = new Thread(new PingHandler());
		thread2.start();
		
		//Thread to listen for scanner requests
		Thread thread3 = new Thread(new CmdListener());
		thread3.start();
		
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