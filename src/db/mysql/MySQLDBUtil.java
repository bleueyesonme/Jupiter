package db.mysql;

// MySQL Server connection URL and parameters.
public class MySQLDBUtil {
	private static final String HOSTNAME = "localhost"; // localhost
	private static final String PORT_NUM = "3306"; // change it to your mysql port number 3306
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "dotson"; // root; dotson
	private static final String PASSWORD = "sptxGD4gmUmWNaX"; // root; sptxGD4gmUmWNaX
	public static final String URL = "jdbc:mysql://"
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}
