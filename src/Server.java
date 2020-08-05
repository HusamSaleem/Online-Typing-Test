import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(5014);
		Socket clientSocket = serverSocket.accept();
		System.out.println(serverSocket.toString());
		System.out.println(clientSocket.getRemoteSocketAddress());
		
		System.out.println("Client Connected!");
		
		int red = -1;
		byte[] buffer = new byte[5*1024]; // a read buffer of 5KiB
		byte[] redData;
		StringBuilder clientData = new StringBuilder();
		String redDataText;
		while ((red = clientSocket.getInputStream().read(buffer)) > -1) {
		    redData = new byte[red];
		    System.arraycopy(buffer, 0, redData, 0, red);
		    redDataText = new String(redData,"UTF-8"); // assumption that client sends data UTF-8 encoded
		    System.out.println("message part recieved:" + redDataText); 
		    clientData.append(redDataText);
		}
		System.out.println("Data From Client :" + clientData.toString());
		
		clientSocket.sendUrgentData(-1);
		
		serverSocket.close();
		clientSocket.close();
	}

}
