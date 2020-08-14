package ServerPackage;

import java.io.IOException;
import java.util.Scanner;

// Listens for user input to display information about the server/database...
public class CmdListener implements Runnable {
	private Scanner scan;

	public CmdListener() {
		scan = new Scanner(System.in);
	}

	@Override
	public void run() {
		showMenu();
	}

	private void showMenu() {
		System.out.println("---------- MENU ----------");
		System.out.println("1. Display how many clients there are (client -c)");
		System.out.println("2. Display all connected clients info (client -i)");
		System.out.println("3. Send data to a client manually (client -msg)");
		System.out.println("4. Send an announcement to all connected clients (server -msg)");
		System.out.println("5. Display all registered users from the Database");
		System.out.println("--------------------------");

		processInput(getInput());
		showMenu();

	}

	private void processInput(String input) {
		if (input.equals("1") || input.equalsIgnoreCase("client -c")) {
			System.out.println("\nThere are: " + getConnectedClientsAmount() + " connected Client(s)");
		} else if (input.equals("2") || input.equalsIgnoreCase("client -i")) {
			System.out.println(displayAllConnectedClientInfo());
		} else if (input.equals("3") || input.equalsIgnoreCase("client -msg")) {
			sendMsgToClient();
		} else if (input.equals("4") || input.equalsIgnoreCase("server -msg")) {
			serverMsg();
		} else if (input.equals("5") || input.equalsIgnoreCase("db user -d")) {
			displayUsers();
		}

	}

	private int getConnectedClientsAmount() {
		return Server.clients.size();
	}

	private String displayAllConnectedClientInfo() {
		String s = "";
		System.out.println("\n---------- Connected Clients ----------");
		for (ClientHandler c : Server.clients) {
			s += c.PROC_ID + ": " + c.S.toString() + "\n";
		}

		return s;
	}

	private boolean sendMsgToClient() {
		if (Server.clients.size() == 0) {
			System.out.println("No connected clients to send a message to");
			return false;
		}

		System.out.println(displayAllConnectedClientInfo());
		System.out.println("Choose a process_id to send to");

		String ID = scan.nextLine();

		for (ClientHandler c : Server.clients) {
			if (c.PROC_ID.equalsIgnoreCase(ID)) {
				System.out.println("Please enter your message to send to the client...");

				String msg = scan.next();

				try {
					return c.sendData("Server Msg: " + msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		return false;
	}

	private boolean serverMsg() {
		System.out.println("Please enter your message to send to all the clients...");

		String msg = "Server: " + scan.nextLine();

		for (ClientHandler c : Server.clients) {
			try {
				c.sendData(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
	}

	private void displayUsers() {
		Server.db.displayAccounts();
	}

	private String getInput() {
		String input = scan.nextLine();

		return input;
	}
}