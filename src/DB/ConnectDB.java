package DB;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3307/pbl4","root","");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
}