import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(5014);
		System.out.println("Server is listening on port: " + serverSocket.getLocalPort());
		
		// Acceps any connections
		Socket clientSocket = serverSocket.accept();
		System.out.println("Client Connected From: " + clientSocket.getRemoteSocketAddress());
		
		System.out.println("Client Connected!");
		
		//System.out.println("Data From Client: " + recieveData(clientSocket));
		Thread thread = new Thread(recieveData(clientSocket));
		thread.start();
		
		Thread thread2 = new Thread(sendData(clientSocket, "Potatoes are awesomee, I agreee"));
		thread2.start();
		
		serverSocket.close();
		clientSocket.close();
	}
	
	private static Runnable sendData(Socket clientSocket, String data) throws IOException {
		PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
		writer.write(data);
		writer.flush();
		return null;
	}
	
//	private static String recieveData(Socket clientSocket) throws IOException {
//		InputStreamReader clientInput = new InputStreamReader(clientSocket.getInputStream());
//		
//		BufferedReader bf = new BufferedReader(clientInput);
//		
//		String data;
//		while ((data = bf.readLine()) != null) {
//			data += bf.readLine();
//		}
//		
//		return data;
//	}
	
	private static String recieveData(Socket clientSocket) throws IOException {
		int red = -1;
		byte[] buffer = new byte[5*1024]; // A read buffer of 5 KiB
		byte[] redData;
		
		StringBuilder clientData = new StringBuilder();
		String redDataText;
		
		// While there is still data available
		while ((red = clientSocket.getInputStream().read(buffer)) > -1) {
			redData = new byte[red];
			System.arraycopy(buffer, 0, redData, 0, red);
			
			redDataText = new String(redData, "UTF-8"); // Assuming the client sends UTF-8 Encoded
			System.out.println(redDataText);
			clientData.append(redDataText);
		}
		
		return clientData.toString();
	}

}
