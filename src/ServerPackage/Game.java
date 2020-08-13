package ServerPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

public class Game {

	private final int MAX_WORDS = 100;
	private final int TIME_DELAY_BEFORE_GAME_START = 5;

	private int id;
	private final int difficulty;
	private HashMap<String, ClientHandler> players;
	// Array list, WPM = index 0, Accuracy = index 1
	private HashMap<String, ArrayList<String>> playerStats;
	private ArrayList<String> playerNames;

	private ArrayList<String> wordList;
	private String wordListAsString;

	private boolean isFinished;

	private boolean gameStarted;

	// In Seconds...
	private int timeLeft;

	public Game(ArrayList<ClientHandler> playerList, int difficulty) {
		this.players = new HashMap<String, ClientHandler>();
		this.playerStats = new HashMap<String, ArrayList<String>>();
		this.playerNames = new ArrayList<String>();
		this.difficulty = difficulty;

		setMaps(playerList);

		this.wordList = new ArrayList<String>();
		this.wordListAsString = "";

		this.isFinished = false;
		this.gameStarted = false;
		this.timeLeft = 60;

		this.id = Server.db.createGameSess(playerList.get(0).getName(), playerList.get(1).getName());

		if (this.id != -1) {
			System.out.println("Game ID: " + this.id + " Successfully created the game session... Players: {"
					+ playerList.get(0).getName() + ", " + playerList.get(1).getName() + "}");

			// Easy
			if (difficulty == 1) {
				startGame("easyWords.txt", playerList.get(0).getName(), playerList.get(1).getName());
			}
		} else {
			System.out.println("Failed to create the game session");
		}

	}

	public void startGame(String fileName, String player1Name, String player2Name) {
		setPlayerGameIds(player1Name, player2Name);
		readFromFile(fileName);
		shuffleWords();
		this.wordListAsString = getWordsAsString();
		sendTimeDelay(TIME_DELAY_BEFORE_GAME_START);
	}

	private void setMaps(ArrayList<ClientHandler> playerList) {

		System.out.println("Size: " + playerList.size());
		for (ClientHandler c : playerList) {
			if (c != null) {
				ArrayList<String> emptyList = new ArrayList<String>();
				this.players.put(c.getName(), c);
				this.playerStats.put(c.getName(), emptyList);
				this.playerNames.add(c.getName());
			}
		}
	}

	private void setPlayerGameIds(String player1Name, String player2Name) {
		players.get(player1Name).setCurGameID(this.id);
		players.get(player2Name).setCurGameID(this.id);
	}

	public void decreaseTimer() {
		this.timeLeft--;

		if (this.timeLeft <= 0) {
			this.isFinished = true;
			updateDatabase();
			resetClientGameInfo();
			this.timeLeft = 0;
		}
	}

	private void updateDatabase() {
		Server.db.updateGameInfo(this.id, this.playerStats, this.playerNames);
	}

	private void resetClientGameInfo() {
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			try {
				c.getValue().sendData("Game Completed");
				c.getValue().setCurGameID(-1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Time in seconds
	public void sendTimeDelay(int time) {
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			try {
				c.getValue().sendData("Game will start in (seconds): " + time);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void notifyClientsGameStarted() {
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			try {
				c.getValue().sendData("Game Started");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		sendWordList();
		this.gameStarted = true;
	}

	public void updateClientData() {
		ClientHandler[] p = new ClientHandler[2];
		int i = 0;
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			updateStats(c.getKey());
			p[i] = c.getValue();
			i++;
		}

		for (i = 0; i < p.length; i++) {
			JSONObject obj = new JSONObject();

			try {
				obj.put("name", p[i].getName());
				obj.put("WPM", getPlayerWPM(p[i].getName()));
				obj.put("accuracy", getPlayerAccuracy(p[i].getName()));
				obj.put("timeLeft", getTimeLeft());

				String jsonText = obj.toString();

				System.out.println("JSON: " + jsonText);

				p[0].sendData("JSON DATA: " + jsonText);
				p[1].sendData("JSON DATA: " + jsonText);
			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendWordList() {
		for (Entry<String, ClientHandler> c : this.players.entrySet()) {
			try {
				c.getValue().sendData("Word List: " + this.wordListAsString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void readFromFile(String txtFile) {
		File file;
		Scanner fileReader = null;
		wordList.clear();
		try {
			file = new File("./TextFiles/" + txtFile);
			fileReader = new Scanner(file);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (fileReader.hasNext()) {
			String word = fileReader.next();
			wordList.add(word);
		}
	}

	private void shuffleWords() {
		Random rand = new Random();

		String temp = null;

		for (int i = 0; i < MAX_WORDS; i++) {
			int randIndex = rand.nextInt(wordList.size());

			temp = wordList.get(i);
			wordList.set(i, wordList.get(randIndex));
			wordList.set(randIndex, temp);
		}
	}

	public String getWordsAsString() {
		String result = "";

		int i = 0;
		for (String s : wordList) {
			result += s + " ";

			i++;
			if (i >= MAX_WORDS)
				break;
		}

		result = result.trim();
		return result;
	}

	public void updateStats(String playerName) {
		String userInput = players.get(playerName).getCurrentInput();

		int wrongIndexCharCount = 0;

		for (int i = 0; i < userInput.length(); i++) {
			if (userInput.charAt(i) != wordListAsString.charAt(i)) {
				wrongIndexCharCount++;
			}
		}

		// Calculate the words per minute
		float grossWPM = (userInput.length() / 5) / ((60 - timeLeft) / 60f);
		float errorRate = wrongIndexCharCount / ((60 - timeLeft) / 60f);
		float netWPM = grossWPM - errorRate;

		if (netWPM < 0)
			netWPM = 0;

		float accuracyCalc = ((userInput.length() - wrongIndexCharCount) / (float) userInput.length()) * 100f;
		accuracyCalc = Math.max(accuracyCalc, 0);

		Math.round(accuracyCalc);
		Math.round(netWPM);

		this.playerStats.get(playerName).add(0, Float.toString(netWPM));
		this.playerStats.get(playerName).add(1, Float.toString(accuracyCalc));
	}

	public boolean playersAreReady() {
		for (Entry<String, ClientHandler> c : this.players.entrySet()) {
			if (!c.getValue().isReady()) {
				System.out.println("Player: " + c.getKey() + " is not ready yet");
				return false;
			}
		}
		return true;
	}

	public boolean isGameStarted() {
		return this.gameStarted;
	}

	public int getGameId() {
		return this.id;
	}

	public int getTimeLeft() {
		return this.timeLeft;
	}

	public void finishGame() {
		this.isFinished = true;
	}

	public String getPlayerWPM(String playerName) {
		return playerStats.get(playerName).get(0);
	}

	public String getPlayerAccuracy(String playerName) {
		return playerStats.get(playerName).get(1);
	}

	public void setPlayerWPM(String playerName, String wpm) {
		playerStats.get(playerName).set(0, wpm);
	}

	public void setPlayerAccuracy(String playerName, String accuracy) {
		playerStats.get(playerName).set(1, accuracy);
	}

	public boolean isGameDone() {
		return this.isFinished;
	}
}