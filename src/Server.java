import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(5014);
		System.out.println("Server is listening on port: " + serverSocket.getLocalPort());
		
		// Acceps any connections
		Socket clientSocket = serverSocket.accept();
		System.out.println("Client Connected From" + clientSocket.getRemoteSocketAddress());
		
		System.out.println("Client Connected!");
		
		//System.out.println("Data From Client :" + RecieveData(clientSocket));
		//SendData(clientSocket, "Potatoes are awesomee, I agreee");
		
		serverSocket.close();
		clientSocket.close();
	}
	
	private static void SendData(Socket clientSocket, String data) throws IOException {
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
		out.writeBytes(data);
		out.flush();
		out.close();
	}
	
	private static String RecieveData(Socket clientSocket) throws IOException {
		int red = -1;
		byte[] buffer = new byte[5*1024]; // A read buffer of 5 KiB
		byte[] redData;
		
		StringBuilder clientData = new StringBuilder();
		String redDataText;
		
		// While there is still data available
		while ((red = clientSocket.getInputStream().read(buffer)) > -1) {
			redData = new byte[red];
			System.arraycopy(buffer, 0, redData, 0, red);
			redDataText = new String(redData, "UTF-8"); // Assumming the client sends UTF-8 Enocded
			clientData.append(redDataText);
		}
		
		return clientData.toString();
	}

}
