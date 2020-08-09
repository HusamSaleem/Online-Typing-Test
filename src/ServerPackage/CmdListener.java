package ServerPackage;

import java.io.IOException;
import java.util.Scanner;

public class CmdListener implements Runnable{
	private Scanner scan;

	public CmdListener() {
		scan = new Scanner(System.in);
	}
	
	@Override
	public void run() {
		showMenu();
		
	}

	private void showMenu() {
		System.out.println("---------- MENU ----------\n");
		System.out.println("1. Display how many clients there are (client -c)");
		System.out.println("2. Display all connected clients info (client -i)");
		System.out.println("3. Send data to a client manually (-client -msg)");
		// System.out.println("4. ");

		processInput(getInput());
		showMenu();

	}

	private void processInput(String input) {
		if (input.equals("1") || input.equalsIgnoreCase("client -c")) {
			System.out.println("There are: " + getConnectedClientsAmount() + " connected Clients");
		} else if (input.equals("2") || input.equalsIgnoreCase("client -i")) {
			System.out.println(displayAllConnectedClientInfo());
		} else if (input.equals("3") || input.equalsIgnoreCase("client -msg")) {
			sendMsgToClient();
		} else {
			System.out.println("Unknown command: " + input);
		}

	}

	private int getConnectedClientsAmount() {
		return Server.clients.size();
	}

	private String displayAllConnectedClientInfo() {
		String s = "";
		for (ClientHandler c : Server.clients) {
			s += c.PROC_ID + ": " + c.S.toString() + "\n";
		}

		return s;
	}

	private boolean sendMsgToClient() {
		System.out.println(displayAllConnectedClientInfo());
		System.out.println("Choose a process_id to send to");
		
		String ID = scan.next();
		
		for (ClientHandler c: Server.clients) {
			if (c.PROC_ID.equalsIgnoreCase(ID)) {
				System.out.println("Please enter your message to send to the client...");
				
				String msg = scan.next();
				
				try {
					return c.sendData(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		return false;
	}

	private String getInput() {
		String input = scan.next();

		return input;
	}
}
