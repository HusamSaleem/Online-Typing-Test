package ServerPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * <b> The main class for the Game Sessions </b>
 * </p>
 * 
 * @author Husam Saleem
 */
public class Game {
	private final int MAX_WORDS = 100;
	private final int TIME_DELAY_BEFORE_GAME_START = 5;
	private final int TIME_PER_GAME = 61;

	private int id;
	private final int difficulty;
	private final int partySize;

	private HashMap<String, ClientHandler> players;

	private ArrayList<String> wordList;
	private String wordListAsString;

	private boolean isFinished;

	private boolean gameStarted;

	// In Seconds...
	private int timeLeft;

	public Game(ArrayList<ClientHandler> playerList, int difficulty, int partySize) {
		this.players = new HashMap<String, ClientHandler>();
		this.partySize = partySize;
		setMaps(playerList);

		this.difficulty = difficulty;
		this.wordList = new ArrayList<String>();
		this.wordListAsString = "";

		this.isFinished = false;
		this.gameStarted = false;
		this.timeLeft = TIME_PER_GAME;
		
		if (partySize == 1) {
			//1 Player Game
			this.id = Server.db.createGameSess(playerList.get(0).getName());
		} else {
			// 2 Player game
			this.id = Server.db.createGameSess(playerList.get(0).getName(), playerList.get(1).getName());
		}

		if (this.id != -1) {
			
			if (partySize == 2)
				System.out.println("Game ID: " + this.id + " Successfully created the game session... Players: {" + playerList.get(0).getName() + ", " + playerList.get(1).getName() + "}");
			else
				System.out.println("Game ID: " + this.id + " Successfully created the game session... Players: {" + playerList.get(0).getName() + "}");

			// 1 = Easy, 2 = Challenging, 3 = Insane
			if (difficulty == 1) {
				startGame("easyWords.txt");
			} else if (difficulty == 2) {
				startGame("challengeWords.txt");
			} else if (difficulty == 3) {
				startGame("insane");
			}
		} else {
			System.out.println("Failed to create the game session");
		}
	}

	/**
	 * <p>
	 * <b> Sets up and starts the game </b>
	 * </p>
	 * 
	 * @param fileName
	 * @param player1Name
	 * @param player2Name
	 */
	public void startGame(String fileName) {
		if (fileName.equals("insane")) {
			generateInsanelyDifficultWordList();
		} else {
			readFromFile(fileName);
		}

		setPlayerGameIds(getPlayerNames());
		shuffleWords();
		this.wordListAsString = getWordsAsString();
		sendTimeDelay(TIME_DELAY_BEFORE_GAME_START);
	}

	/**
	 * <p>
	 * <b> A helper method to initialize the arrays </b>
	 * </p>
	 * 
	 * @param playerList
	 */
	private void setMaps(ArrayList<ClientHandler> playerList) {
		System.out.println("Player size: " + playerList.size());
		for (ClientHandler c : playerList) {
			if (c != null) {
				this.players.put(c.getName(), c);
			}
		}
	}

	/**
	 * <p>
	 * <b> Sets the game ids of the game session for players </b>
	 * </p>
	 * 
	 * @param player1Name
	 * @param player2Name
	 */
	private void setPlayerGameIds(String[] names) {
		for (String s : names) {
			players.get(s).setCurGameID(this.id);
		}
	}

	/**
	 * <p>
	 * <b> Decreases the game timer and also checks to see if it has reached the end
	 * </b>
	 * </p>
	 * <p>
	 * <b> If it reached the end, it will update the game session's data in the
	 * database and reset the clients info regarding this game </b>
	 * </p>
	 */
	public void decreaseTimer() {
		this.timeLeft--;

		if (this.timeLeft <= 0) {
			this.isFinished = true;
			updateDatabase();
			resetClientGameInfo();
			this.timeLeft = 0;
		}
	}

	/**
	 * <p>
	 * <b> Updates the game session's data row with the player statistics results'
	 * </b>
	 * </p>
	 */
	private void updateDatabase() {
		Server.db.updateGameInfo(this.id, this.players, this.getPartySize(), this.difficulty);
	}

	/**
	 * <p>
	 * <b> Reset some variables for each player </b>
	 * </p>
	 */
	private void resetClientGameInfo() {
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			try {
				c.getValue().sendData("Game Completed");
				c.getValue().setCurGameID(-1);
				c.getValue().setFinishedTyping(false);
				c.getValue().setReady(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>
	 * <b> Sets a time delay before the game actually starts </b>
	 * </p>
	 * 
	 * @param time (SECONDS)
	 */
	public void sendTimeDelay(int time) {
		for (Entry<String, ClientHandler> c : players.entrySet()) {
			try {
				c.getValue().sendData("Game will start in (seconds): " + time);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>
	 * <b> Notifies all players that the game has started </b>
	 * </p>
	 */
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

	/**
	 * <p>
	 * <b> Sends the created word list to the players </b>
	 * </p>
	 */
	public void sendWordList() {
		for (Entry<String, ClientHandler> c : this.players.entrySet()) {
			try {
				c.getValue().sendData("Word List: " + this.wordListAsString);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <p>
	 * <b> A helper method to read from text files </b>
	 * </p>
	 * 
	 * @param txtFile
	 */
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

	/**
	 * <p>
	 * <b> A helper method to shuffle the word list at random </b>
	 * </p>
	 */
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

	/**
	 * <p>
	 * <b> Returns the word list as a string </b>
	 * </p>
	 * 
	 * @return String
	 */
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

	/**
	 * <p>
	 * <b> Generate the most difficult word list </b>
	 * </p>
	 */
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

	/**
	 * <p>
	 * <b> Updates the clients info regarding their statistics from the game</b>
	 * </p>
	 * <p>
	 * <b> Also sends a JSON string containing all player's statistics to all
	 * players </b>
	 * </p>
	 */
	public void updateClientData() {
		Iterator<Entry<String, ClientHandler>> iter = this.players.entrySet().iterator();

		String[] jsonStrings = new String[this.players.size()];

		int i = 0;
		while (iter.hasNext()) {
			Entry<String, ClientHandler> entry = iter.next();

			if (entry.getValue().isConnected()) {
				updateStats(entry.getKey());
				JSONObject obj = new JSONObject();

				try {
					obj.put("name", entry.getKey());
					obj.put("WPM", getPlayerWPM(entry.getKey()));
					obj.put("accuracy", getPlayerAccuracy(entry.getKey()));
					obj.put("timeLeft", getTimeLeft());

					jsonStrings[i] = obj.toString();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			i++;
		}

		iter = this.players.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<String, ClientHandler> entry = iter.next();

			for (String s : jsonStrings) {
				if (entry.getValue().isConnected())
					try {
						entry.getValue().sendData("JSON DATA STATS: " + s);
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	/**
	 * <p>
	 * <b> Update the statistics (WPM, Accuracy) of a certain player </b>
	 * </p>
	 * 
	 * @param playerName
	 */
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
			float grossWPM = (userInput.length() / 5) / ((60 - players.get(playerName).getTimeFinished()) / 60f);
			float errorRate = wrongIndexCharCount / ((60 - players.get(playerName).getTimeFinished()) / 60f);
			float netWPM = grossWPM - errorRate;

			if (netWPM < 0)
				netWPM = 0;

			netWPM = Math.round(netWPM);

			this.players.get(playerName).getPlayerStats().setWpm(Float.toString(netWPM));
		} else {
			// Calculate the words per minute
			float grossWPM = (userInput.length() / 5) / ((60 - timeLeft) / 60f);
			float errorRate = wrongIndexCharCount / ((60 - timeLeft) / 60f);
			float netWPM = grossWPM - errorRate;

			if (netWPM < 0)
				netWPM = 0;

			netWPM = Math.round(netWPM);

			this.players.get(playerName).getPlayerStats().setWpm(Float.toString(netWPM));
		}

		float accuracyCalc = ((userInput.length() - wrongIndexCharCount) / (float) userInput.length()) * 100f;
		accuracyCalc = Math.max(accuracyCalc, 0);
		accuracyCalc = Math.round(accuracyCalc);

		this.players.get(playerName).getPlayerStats().setAccuracy(Float.toString(accuracyCalc));
	}

	/**
	 * @return a list of the player names in the game session
	 */
	public String[] getPlayerNames() {
		Iterator<Entry<String, ClientHandler>> iter = this.players.entrySet().iterator();

		String[] names = new String[this.players.size()];

		int i = 0;
		while (iter.hasNext()) {
			Entry<String, ClientHandler> entry = iter.next();
			names[i] = entry.getKey();
			i++;
		}

		return names;
	}

	/**
	 * <p>
	 * <b> Checks to see if the players are ready </b>
	 * </p>
	 * 
	 * @return
	 */
	public boolean playersAreReady() {
		for (Entry<String, ClientHandler> c : this.players.entrySet()) {
			if (!c.getValue().isReady()) {
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
		return Integer.toString(this.players.get(playerName).getPlayerStats().getWpm());
	}

	public String getPlayerAccuracy(String playerName) {
		return Integer.toString(this.players.get(playerName).getPlayerStats().getAccuracy());
	}

	public boolean isGameDone() {
		return this.isFinished;
	}

	public int getPartySize() {
		return partySize;
	}
}