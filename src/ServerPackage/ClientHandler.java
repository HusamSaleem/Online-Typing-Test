package ServerPackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
	public final Socket S;
	public final String PROC_ID;

	private String playerName;

	private int retryConnections;
	private boolean isConnected;
	private long lastPinged;

	public ClientHandler(Socket S, String PROC_ID) {
		this.S = S;
		this.PROC_ID = PROC_ID;

		this.retryConnections = 0;
		this.isConnected = true;
		this.lastPinged = System.currentTimeMillis();

		try {
			sendData("Process_ID: " + this.PROC_ID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		PrintWriter writer = new PrintWriter(S.getOutputStream(), true);

		if (writer.checkError())
			return false;

		// "`" means its the end of the data line
		writer.println(data + "`");
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

				} else if (d.equals("Process_ID: NULL")) {

					this.lastPinged = System.currentTimeMillis();
					sendData("Process_ID: " + this.PROC_ID);

				} else if (d.contains("Register Username: ")) {

					String name = d.substring(19).trim();
					if (Server.db.registerUsername(name.toLowerCase())) {
						System.out.println(name + " has been sucessfully registered");
						sendData("Register Success");

						this.playerName = name;
					} else {
						sendData("Register Failure");
					}
				}
			}
		}
	}

	// Returns false : Delete this client b/c its inactive
	// Returns true: its still not exceeded the retry maximum limit
	public boolean keepAlive() {
		if (this.retryConnections > 3) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isAlive() {
		return this.isConnected;
	}

	public int getRetryCount() {
		return this.retryConnections;
	}

	public void increaseRetryCount() {
		this.retryConnections++;
	}

	public long getLastPingTime() {
		return this.lastPinged;
	}

	public String getName() {
		return this.playerName;
	}
}
