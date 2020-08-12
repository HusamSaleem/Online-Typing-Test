package ServerPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
			//Statement statement = dbConn.createStatement();

			PreparedStatement statement = dbConn.prepareStatement("INSERT INTO 2PlayerGames(Player_One_WPM, Player_Two_WPM, Player_One_Accuracy, Player_Two_Accuracy, Player_One_Name, Player_Two_Name) " + "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
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

}
