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

	public ClientHandler(Socket S, String PROC_ID) {
		this.S = S;
		this.PROC_ID = PROC_ID;
	}

	@Override
	public void run() {
		try {
			sendData("Process_ID: " + this.PROC_ID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String[] data = null;
		try {
			data = recieveData();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.out.println("Something went wrong... with client: " + S.toString());
			return;
		}

		if (!data[0].equals("-1")) {

			for (String d : data)
				System.out.println("Client: " + S.toString() + " sent this data: " + d);
		}

//		while (true) {
//			try {
//				String[] data = recieveData();
//
//				if (!data[0].equals("-1")) {
//
//					for (String d : data)
//						System.out.println("Client: " + s.toString() + " sent this data: " + d);
//				}
//
//				Thread.sleep(250);
//			} catch (IOException | InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				break;
//			}
//		}
//
//		try {
//			s.close();
//			System.out.println("Client " + s.toString() + " has disconnected");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public boolean sendData(String data) throws IOException {
		System.out.println("Sending data");
		PrintWriter writer = new PrintWriter(S.getOutputStream());

		if (writer.checkError())
			return false;
		writer.println(data);
		writer.flush();
		System.out.println("Data has been sent!");
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

			redDataText = new String(redData, "UTF-8"); // Assuming the client sends UTF-8 Encoded

			clientData += redDataText;

			if (clientData.indexOf("`") != -1) {
				break;
			}
		}

		// Turn all the sent commands into an array in case if they get combined
		String[] data = clientData.split("`");
		clientData = clientData.replaceAll("`", "");
		return data;
	}
}