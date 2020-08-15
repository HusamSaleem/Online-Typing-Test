package ServerPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * <p><b> Listens for the user input to display information about the server/database </b></p>
 * @author Husam Saleem
 *
 */
public class CmdListener implements Runnable {
	private Scanner scan;

	public CmdListener() {
		scan = new Scanner(System.in);
	}

	@Override
	public void run() {
		showMenu();
	}

	
	/**
	 * <p><b> Displays the menu for available choices to choose </b></p>
	 */
	private void showMenu() {
		System.out.println("---------- MENU ----------");
		System.out.println("1. Display how many clients there are (client -c)");
		System.out.println("2. Display all connected clients info (client -i)");
		System.out.println("3. Send data to a client manually (client -msg)");
		System.out.println("4. Send an announcement to all connected clients (server -msg)");
		System.out.println("5. Display all registered users from the Database (db user -d)");
		System.out.println("6. Display all games from the Database (db game -d)");
		System.out.println("7. Display number of current active games (server activeCount -g)");
		System.out.println("8. Display all active game session info (server active -g)");
		System.out.println("--------------------------");

		processInput(getInput());
		showMenu();

	}

	/**
	 * <p><b> Processes the user input from the Scanner and applies the methods relative</b></p>
	 * @param input
	 */
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
		} else if (input.equals("6") || input.equalsIgnoreCase("db game -d")) {
			displayGames();
		} else if (input.equals("7") || input.equalsIgnoreCase("server activeCount -g")) {
			displayNumberOfActiveGames();
		} else if (input.equals("8") || input.equalsIgnoreCase("server active -g")) {
			displayAllActiveGameInfo();
		}
	}

	/**
	 * <p><b> Returns the total amount of clients that have connected to the server </b></p>
	 * @return
	 */
	private int getConnectedClientsAmount() {
		return Server.clients.size();
	}

	/**
	 * <p><b> Displays all the connected clients information </b></p>
	 * @return String 
	 */
	private String displayAllConnectedClientInfo() {
		String s = "";
		System.out.println("\n---------- Connected Clients ----------");
		for (ClientHandler c : Server.clients) {
			s += c.PROC_ID + ": " + c.S.toString() + ", Name: " + c.getName() + "\n";
		}

		return s;
	}

	/**
	 * <p><b> Sends a message to a specific client by process id </b></p>
	 * @return true if it worked
	 */
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
					e.printStackTrace();
				}

			}
		}
		return false;
	}

	/**
	 * Sends a server wide announcement to all connected clients
	 * @return
	 */
	private boolean serverMsg() {
		System.out.println("Please enter your message to send to all the clients...");

		String msg = scan.nextLine();

		for (ClientHandler c : Server.clients) {
			try {
				c.sendData("Server Msg: " + msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 * <p><b> Displays all registered accounts </b></p>
	 */
	private void displayUsers() {
		Server.db.displayAccounts();
	}

	/**
	 * Gets the scanner input
	 * @return
	 */
	private String getInput() {
		String input = scan.nextLine();

		return input;
	}

	/**
	 * <p><b> Displays all the created game session from the db </b></p>
	 */
	private void displayGames() {
		Server.db.displayAllGames();
	}

	/**
	 * <p><b> Displays the number of active games </b></p>
	 */
	private void displayNumberOfActiveGames() {
		int activeGameSessions = Server.mmService.activeGameSessions.size();

		System.out.println("Number of active Game Sessions: " + activeGameSessions);
	}

	/**
	 * <p><b> Displays all active game session information </b></p>
	 */
	private void displayAllActiveGameInfo() {
		System.out.println("---------ALL ACTIVE GAMES----------");

		Iterator<Entry<Integer, Game>> iter = Server.mmService.activeGameSessions.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<Integer, Game> entry = iter.next();

			int id = entry.getValue().getGameId();
			int timeLeft = entry.getValue().getTimeLeft();
			ArrayList<String> names = entry.getValue().getPlayerNames();

			String nameStr = "";
			for (String s : names)
				nameStr += " | " + s;

			System.out.println("|Game ID: " + id + " | Players: " + nameStr + "|");
		}

		System.out.println("-----------------------------------");
	}
}