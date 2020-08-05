import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
	static ServerSocket serverSocket;

	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = new Server();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				server.shutDown();
			}
		});

		serverSocket = new ServerSocket(5014);
		System.out.println("Server is listening on port: " + serverSocket.getLocalPort());

		while (true) {
			// Accepts any connections
			Socket clientSocket = serverSocket.accept();
			System.out.println("Client Connected: " + clientSocket.toString());

			// Add client to existing client list
			ClientHandler cl = new ClientHandler(clientSocket);
			Thread thread = new Thread(cl);

			thread.start();
			clients.add(cl);
			
			deleteInactiveClients();
		}
		
	}
	
	private static void deleteInactiveClients() {
		for (ClientHandler c : clients) {
			if (c.s.isClosed() || !c.s.isConnected()) {
				clients.remove(c);
			}
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

class ClientHandler extends Thread {
	public final Socket s;

	public ClientHandler(Socket s) {
		this.s = s;
	}

	@Override
	public void run() {
		try {
			sendData("Hello");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			System.out.println("Client: " + s.toString() + " sent this data: " + recieveData());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		while (!s.isClosed()) {
//			try {
//				System.out.println("Client: " + s.toString() + " sent this data: " + recieveData());
//				Thread.sleep(250);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				break;
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				break;
//			}
//		}

		try {
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendData(String data) throws IOException {
		System.out.println("Sending data");
		PrintWriter writer = new PrintWriter(s.getOutputStream());
		writer.println(data);
		writer.flush();
		System.out.println("Data has been sent!");
	}

	public String recieveData() throws IOException, InterruptedException {
		int red = -1;
		byte[] buffer = new byte[5 * 1024]; // A read buffer of 5 KiB
		byte[] redData;

		StringBuilder clientData = new StringBuilder();
		String redDataText;

		// While there is still data available
		while ((red = s.getInputStream().read(buffer)) > -1) {
			redData = new byte[red];
			System.arraycopy(buffer, 0, redData, 0, red);

			redDataText = new String(redData, "UTF-8"); // Assuming the client sends UTF-8 Encoded
			
			clientData.append(redDataText);
		}
		return clientData.toString();
	}

}
