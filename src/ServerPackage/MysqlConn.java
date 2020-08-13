package ServerPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class MysqlConn {

	Connection dbConn = null;

	private String url = "jdbc:mysql://localhost:3306/TypingTestDb?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private String username = "PotatoAdmin";
	private String password = "Popakon123!";

	public MysqlConn() {

		startConnection();
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean registerUsername(String user) {
		try {
			Statement statement = dbConn.createStatement();
			int result = statement.executeUpdate("INSERT INTO accounts(Name) VALUES('" + user + "')");

			if (result > 0)
				return true;
			else
				return false;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());

			return false;
		}
	}

	public int createGameSess(String player1Name, String player2Name) {
		try {
			// Statement statement = dbConn.createStatement();

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

	public boolean updateGameInfo(int id, HashMap<String, ArrayList<String>> playerStats,
			ArrayList<String> playerNames) {
		try {
			String sql = "UPDATE 2PlayerGames SET " + "Player_One_WPM = ?, " + "Player_Two_WPM = ?, "
					+ "Player_One_Accuracy = ?, " + "Player_Two_Accuracy = ? " + "WHERE Game_ID = ?";
			PreparedStatement statement = dbConn.prepareStatement(sql);

			// WPM
			statement.setInt(1, (int) Math.round(Integer.parseInt(playerStats.get(playerNames.get(0)).get(0))));
			statement.setInt(2, (int) Math.round(Integer.parseInt(playerStats.get(playerNames.get(1)).get(0))));

			// Accuracy
			statement.setInt(3, (int) Math.round(Integer.parseInt(playerStats.get(playerNames.get(0)).get(1))));
			statement.setInt(4, (int) Math.round(Integer.parseInt(playerStats.get(playerNames.get(1)).get(1))));

			statement.setInt(5, id);

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

}
