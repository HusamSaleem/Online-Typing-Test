package ServerPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConn {
	
	Connection dbConn = null;
	
	private String url = "jdbc:mysql://localhost:3306/TypingTestDb";
	private String username = "PotatoAdmin";
	private String password = "Popakon123!";
	
	public MysqlConn() {
		
		startConnection();
	}

	public static void main(String[] args) {
		MysqlConn mysql = new MysqlConn();
	}
	
	
	private void startConnection() {
		try { 
			dbConn = DriverManager.getConnection(url, username, password);
			
			if (dbConn != null)
				System.out.println("Connectionn has been established!");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} 
	}

}
