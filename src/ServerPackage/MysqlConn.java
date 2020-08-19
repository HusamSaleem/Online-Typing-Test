package ServerPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * <p>
 * <b> This is the database class where it handles the connectivity, as well as
 * the saving/updating/getting information </b>
 * </p>
 * 
 * @author Husam Saleem
 */
public class MysqlConn {
	Connection dbConn = null;

	// CHANGE ME
	private String dbName = "DATABASENAME";
	private String url = "jdbc:mysql://localhost:3306/"+ dbName + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private String username = "USER";
	private String password = "PASSWORD";

	public MysqlConn() {
		startConnection();
	}

	/**
	 * <p>
	 * <b> Starts the connection from the server to the database </b>
	 * </p>
	 */
	private void startConnection() {
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");
			dbConn = DriverManager.getConnection(url, username, password);

			if (dbConn != null)
				System.out.println("Database Connection has been established!");
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * <b> This will get all the registered usernames from the database (Table:
	 * accounts) and display it </b>
	 * </p>
	 */
	public void displayAccounts() {
		try {
			Statement statement = dbConn.createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM accounts ORDER BY ID");

			System.out.println("----------ALL ACCOUNTS CURRENTLY REGISTERED----------");
			while (result.next()) {
				// Retrieve the column for each row
				String name = result.getString("Name");
				System.out.println("ID: " + result.getInt("ID") + ", Name: " + name);
			}

			System.out.println("-----------------------------------------------------");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * <b> Tries to register a unique name into the database </b>
	 * </p>
	 * 
	 * @param user
	 * @return true if it worked
	 */
	public boolean registerUsername(String user) {
		try {
			Statement statement = dbConn.createStatement();
			int result = statement.executeUpdate("INSERT INTO accounts(Name) VALUES('" + user + "')");

			if (result > 0)
				return true;
			else
				return false;

		} catch (SQLException e) {
			System.out.println(e.getMessage());

			return false;
		}
	}

	/**
	 * <p>
	 * <b> This creates a new row (Table of 1PlayerGames) in the database for the
	 * new game session </b>
	 * </p>
	 * 
	 * @param player1Name
	 * @return the Game ID of the created session
	 */
	public int createGameSess(String player1Name) {
		try {
			PreparedStatement statement = dbConn
					.prepareStatement("INSERT INTO 1PlayerGames(Player_One_WPM, Player_One_Accuracy, Player_One_Name) "
							+ "VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, 0);
			statement.setInt(2, 0);
			statement.setString(3, player1Name);

			int result = statement.executeUpdate();

			ResultSet res = statement.getGeneratedKeys();

			if (result == 1) {
				if (res.next()) {
					return res.getInt(1);
				}
			} else {
				System.out.println("Couldn't create the game session");
				return -1;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());

			return -1;
		}
		System.out.println("Couldn't create the game session");
		return -1;
	}

	/**
	 * <p>
	 * <b> This creates a new row (Table of 2PlayerGames) in the database for the
	 * new game session </b>
	 * </p>
	 * 
	 * @param player1Name
	 * @param player2Name
	 * @return the Game ID of the created session
	 */
	public int createGameSess(String player1Name, String player2Name) {
		try {
			PreparedStatement statement = dbConn.prepareStatement(
					"INSERT INTO 2PlayerGames(Player_One_WPM, Player_Two_WPM, Player_One_Accuracy, Player_Two_Accuracy, Player_One_Name, Player_Two_Name) "
							+ "VALUES (?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, 0);
			statement.setInt(2, 0);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setString(5, player1Name);
			statement.setString(6, player2Name);

			int result = statement.executeUpdate();

			ResultSet res = statement.getGeneratedKeys();

			if (result == 1) {
				if (res.next()) {
					return res.getInt(1);
				}
			} else {
				System.out.println("Couldn't create the game session");
				return -1;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());

			return -1;
		}
		System.out.println("Couldn't create the game session");
		return -1;
	}

	/**
	 * <p><b> Updates the player statistics for the game session for when the game is completed (2 Player Games & 1 Player Games) </b></p>
	 * @param id (The game ID)
	 * @param playerStats (The player statistics for the game session)
	 * @param playerNames
	 * @return true if it worked
	 */
	public boolean updateGameInfo(int id, HashMap<String, ClientHandler> playerStats, int gameSize, int difficulty) {
		try {
			String sql = "";
			if (gameSize == 2)
				sql = "UPDATE 2PlayerGames SET " + "Player_One_WPM = ?, " + "Player_Two_WPM = ?, " + "Player_One_Accuracy = ?, " + "Player_Two_Accuracy = ?, " + "Game_Difficulty = ? " + "WHERE Game_ID = ?";
			else {
				sql = "UPDATE 1PlayerGames SET " + "Player_One_WPM = ?, " + "Player_One_Accuracy = ?, " + "Game_Difficulty = ? " + "WHERE Game_ID = ?";
			}
			
			PreparedStatement statement = dbConn.prepareStatement(sql);
			
			Iterator<Entry<String, ClientHandler>> iter = playerStats.entrySet().iterator();
			
			while (iter.hasNext()) {
				Entry<String, ClientHandler> entry = iter.next();
				
				if (gameSize == 2) {
					if (entry.getValue().playerID == 1) {
						statement.setInt(1, entry.getValue().getPlayerStats().getWpm());
						statement.setInt(3, entry.getValue().getPlayerStats().getAccuracy());
					} else if (entry.getValue().playerID == 2) {
						statement.setInt(2, entry.getValue().getPlayerStats().getWpm());
						statement.setInt(4, entry.getValue().getPlayerStats().getAccuracy());
					}
				} else {
					statement.setInt(1, entry.getValue().getPlayerStats().getWpm());
					statement.setInt(2, entry.getValue().getPlayerStats().getAccuracy());
				}
			}
			if (gameSize == 2) {
				// Game ID
				statement.setInt(6, id);
				statement.setInt(5, difficulty);
			} else {
				statement.setInt(4, id);
				statement.setInt(3, difficulty);
			}

			int result = statement.executeUpdate();

			if (result == 1) {
				System.out.println("Successfully updated the stats of Game_ID: " + id);
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	/**
	 * <p>
	 * <b> Gets all the game sessions that have been created from the database and
	 * displays it </b>
	 * </p>
	 */
	public void displayAllGames() {
		try {
			Statement statement = dbConn.createStatement();

			String sql = "SELECT * FROM 2PlayerGames";

			ResultSet result = statement.executeQuery(sql);

			System.out.println("----------ALL 2 PLAYER GAME SESSIONS----------");
			while (result.next()) {
				int id = result.getInt(1);
				int playerOneWpm = result.getInt("Player_One_WPM");
				int playerOneAcc = result.getInt("Player_One_Accuracy");
				int playerTwoWpm = result.getInt("Player_Two_WPM");
				int playerTwoAcc = result.getInt("Player_Two_Accuracy");
				int difficulty = result.getInt("Game_Difficulty");

				String playerOneName = result.getString("Player_One_Name");
				String playerTwoName = result.getString("Player_Two_Name");

				System.out.println("|Game ID: " + id + " | " + "Difficulty: " + difficulty + " | " + "Player 1 Name: " + playerOneName + " | "
						+ "Player Two Name: " + playerTwoName + " | " + " Player One WPM: " + playerOneWpm + " | "
						+ "Player One Accuracy: " + playerOneAcc + "% | " + " Player Two WPM: " + playerTwoWpm + " | "
						+ "Player Two Accuracy: " + playerTwoAcc + "%|");
			}
			System.out.println("----------------------------------------------");
			
			sql = "SELECT * FROM 1PlayerGames";

			result = statement.executeQuery(sql);

			System.out.println("----------ALL 1 PLAYER GAME SESSIONS----------");
			while (result.next()) {
				int id = result.getInt(1);
				int playerOneWpm = result.getInt("Player_One_WPM");
				int playerOneAcc = result.getInt("Player_One_Accuracy");
				int difficulty = result.getInt("Game_Difficulty");

				String playerOneName = result.getString("Player_One_Name");

				System.out.println("|Game ID: " + id + " | " + "Difficulty: " + difficulty + " | " +  "Player 1 Name: " + playerOneName + " | " + " Player One WPM: " + playerOneWpm + " | " + "Player One Accuracy: " + playerOneAcc);
			}

			System.out.println("----------------------------------------------");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}