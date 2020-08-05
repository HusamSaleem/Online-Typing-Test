import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(5014);
		Socket socket = serverSocket.accept();
		System.out.println(serverSocket.toString());
		System.out.println(socket.getRemoteSocketAddress());
		
		System.out.println("Client Connected!");
		
		System.out.println(socket.getInputStream());
		socket.sendUrgentData(-1);
		
		serverSocket.close();
		socket.close();
	}

}
