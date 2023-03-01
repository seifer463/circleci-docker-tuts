package au.com.blueoak.portal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * {insert details}
 * <br><br>
 * <b>(c)2015 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class TestDatabase {

    /** JDBC driver to be used */
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    
    /** a single instance of this class */
    private static TestDatabase INSTANCE;

    /** current active connection */
    private Connection connection;
	
    /** 
     * Constructor
     * */
	public TestDatabase() throws ClassNotFoundException {

		// register JDBC driver
		Class.forName(JDBC_DRIVER);
	}
    
    /** 
     * Let's open the connection into the database
     * */
	public static void open(String dbURL, String userName, String password)
			throws ClassNotFoundException, SQLException {

		if (INSTANCE != null) {
			// there is already an instance, ensure any active connection is closed
			close();
		} else {
			// no instance is available, so we will create one
			INSTANCE = new TestDatabase();
		}
		// open connection to the required database
		INSTANCE.connection = DriverManager.getConnection(dbURL, userName, password);
	}
    
    /**
     * Close if there's an active database connection
     *  */
	public static void close() {

		if (INSTANCE != null && INSTANCE.connection != null) {
			try {
				// there is an active connection, close it
				INSTANCE.connection.close();
			} catch (SQLException e) {
			}
		}
	}
	
	/**
	 * @return the INSTANCE
	 */
	public static TestDatabase getInstance() {
		
		return INSTANCE;
	}

	/**
	 * @param INSTANCE the INSTANCE to set
	 */
	public static void setInstance(TestDatabase instance) {
		
		INSTANCE = instance;
	}

	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		
		this.connection = connection;
	}
	

}