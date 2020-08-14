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
	private final int TIME_PER_GAME = 60;

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
		this.timeLeft = TIME_PER_GAME;

		this.id = Server.db.createGameSess(playerList.get(0).getName(), playerList.get(1).getName());

		if (this.id != -1) {
			System.out.println("Game ID: " + this.id + " Successfully created the game session... Players: {"
					+ playerList.get(0).getName() + ", " + playerList.get(1).getName() + "}");

			// 1 = Easy, 2 = Challenging, 3 = Insane
			if (difficulty == 1) {
				startGame("easyWords.txt", playerList.get(0).getName(), playerList.get(1).getName());
			} else if (difficulty == 2) {
				startGame("challengeWords.txt", playerList.get(0).getName(), playerList.get(1).getName());
			} else if (difficulty == 3) {
				startGame("insane", playerList.get(0).getName(), playerList.get(1).getName());
			}
		} else {
			System.out.println("Failed to create the game session");
		}
	}

	public void startGame(String fileName, String player1Name, String player2Name) {
		if (fileName.equals("insane")) {
			generateInsanelyDifficultWordList();
		} else {
			readFromFile(fileName);
		}

		setPlayerGameIds(player1Name, player2Name);
		shuffleWords();
		this.wordListAsString = getWordsAsString();
		sendTimeDelay(TIME_DELAY_BEFORE_GAME_START);
	}

	private void setMaps(ArrayList<ClientHandler> playerList) {

		System.out.println("Player size: " + playerList.size());
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
				c.getValue().setFinishedTyping(false);
			} catch (IOException e) {
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
				e.printStackTrace();
			}
		}
	}

	public void notifyClientsGameStarted() {
		sendWordList();
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			try {
				c.getValue().sendData("Game Started");
				c.getValue().sendData("Time Left: " + this.getTimeLeft());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.gameStarted = true;
	}

	public void updateClientData() {
		ClientHandler[] p = new ClientHandler[2];
		int i = 0;
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			if (!c.getValue().S.isClosed()) {
				updateStats(c.getKey());
				p[i] = c.getValue();
			} else {
				p[i] = null;
			}
			i++;
		}

		for (i = 0; i < p.length; i++) {
			if (p[i] != null) {
				JSONObject obj = new JSONObject();

				try {
					obj.put("name", p[i].getName());
					obj.put("WPM", getPlayerWPM(p[i].getName()));
					obj.put("accuracy", getPlayerAccuracy(p[i].getName()));
					obj.put("timeLeft", getTimeLeft());

					String jsonText = obj.toString();

					p[0].sendData("JSON DATA STATS: " + jsonText);
					p[1].sendData("JSON DATA STATS: " + jsonText);
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendWordList() {
		for (Entry<String, ClientHandler> c : this.players.entrySet()) {
			try {
				c.getValue().sendData("Word List: " + this.wordListAsString);
			} catch (IOException e) {
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

	private void generateInsanelyDifficultWordList() {
		wordList.clear();

		Random randLength = new Random();
		Random randIndex = new Random();
		Random randRoll = new Random();

		String[] alphabet = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
				"s", "t", "u", "v", "w", "x", "y", "z" };
		String[] specialCharacters = { ",", ";", "!", "@", "$", "&", "%", "#", "*", "(", ")" };

		final int MAX_WORD_LENGTH = 12;
		final int MIN_WORD_LENGTH = 5;
		final int SPECIAL_CHARACTER_CHANCE = 15;
		final int UPPERCASE_CHANCE = 25;

		for (int i = 0; i < MAX_WORDS; i++) {
			int length = randLength.nextInt(MAX_WORD_LENGTH - MIN_WORD_LENGTH) + MIN_WORD_LENGTH;
			String word = "";

			for (int j = 0; j < length; j++) {
				int specialCharacterChance = randRoll.nextInt(101);
				int index = 0;

				if (SPECIAL_CHARACTER_CHANCE >= specialCharacterChance) {
					index = randIndex.nextInt(specialCharacters.length);
					word += specialCharacters[index];
				} else {
					index = randIndex.nextInt(alphabet.length);

					int upperCaseChance = randRoll.nextInt(101);

					if (UPPERCASE_CHANCE >= upperCaseChance) {
						word += alphabet[index].toUpperCase();
					} else {
						word += alphabet[index].toLowerCase();
					}
				}
			}

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
		boolean finishedTyping = players.get(playerName).isFinishedTyping();

		int wrongIndexCharCount = 0;

		for (int i = 0; i < userInput.length(); i++) {
			if (userInput.charAt(i) != wordListAsString.charAt(i)) {
				wrongIndexCharCount++;
			}
		}

		if (finishedTyping) {
			// Calculate the words per minute
			float grossWPM = (userInput.length() / 5) / ((players.get(playerName).getTimeFinished()) / 60f);
			float errorRate = wrongIndexCharCount / ((players.get(playerName).getTimeFinished()) / 60f);
			float netWPM = grossWPM - errorRate;

			if (netWPM < 0)
				netWPM = 0;

			netWPM = Math.round(netWPM);

			this.playerStats.get(playerName).add(0, Float.toString(netWPM));
		} else {
			// Calculate the words per minute
			float grossWPM = (userInput.length() / 5) / ((60 - timeLeft) / 60f);
			float errorRate = wrongIndexCharCount / ((60 - timeLeft) / 60f);
			float netWPM = grossWPM - errorRate;

			if (netWPM < 0)
				netWPM = 0;

			netWPM = Math.round(netWPM);

			this.playerStats.get(playerName).add(0, Float.toString(netWPM));
		}
		
		float accuracyCalc = ((userInput.length() - wrongIndexCharCount) / (float) userInput.length()) * 100f;
		accuracyCalc = Math.max(accuracyCalc, 0);

		accuracyCalc = Math.round(accuracyCalc);
		
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