package ServerPackage;

import java.sql.Connection;
import java.sql.DriverManager;
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
		displayAccounts();
		System.out.println(registerUsername("Test"));
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

	private void displayAccounts() {
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

	private boolean registerUsername(String user) {
		try {
			Statement statement = dbConn.createStatement();
			int result = statement.executeUpdate("INSERT INTO accounts(Name) VALUES('" + user + "')");

			if (result > 0)
				return true;
			else
				return false;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

	}

}
