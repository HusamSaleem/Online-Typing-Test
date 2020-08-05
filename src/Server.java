import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(5014);
		Socket socket = serverSocket.accept();
		System.out.println(serverSocket.toString());
		System.out.println(socket.getRemoteSocketAddress());
		
		System.out.println("Client Connected!");
		
		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		BufferedReader bf = new BufferedReader(in);
		
		System.out.println(bf.readLine());
		socket.sendUrgentData(-1);
		
		serverSocket.close();
		socket.close();
	}

}
