package ServerPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConn {
	
	Connection dbConn = null;
	
	private String url = "jdbc:mysql://localhost:3306/TypingTestDb?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	private String username = "PotatoAdmin";
	private String password = "Popakon123!";
	
	public MysqlConn() {
		
		startConnection();
	}

	public static void main(String[] args) {
		//MysqlConn mysql = new MysqlConn();
	}
	
	
	private void startConnection() {
		try { 
			
			Class.forName("com.mysql.jdbc.Driver");
			dbConn = DriverManager.getConnection(url, username, password);
			
			if (dbConn != null)
				System.out.println("Connectionn has been established!");
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

}
