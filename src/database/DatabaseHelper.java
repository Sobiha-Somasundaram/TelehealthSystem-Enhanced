package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fixed DatabaseHelper with proper schema updates
 */
public class DatabaseHelper {
    //private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/telehealth.db";
    private static final String URL = "jdbc:mysql://localhost:3306/telehealth_system";
    private static final String USER = "root";
    static final String PASSWORD = "1234";

    /**
     *
     */
    public static com.sun.jdi.connect.spi.Connection getConnection;
    
    /**
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to load MySQL JDBC driver");
            e.printStackTrace();
        }

        // Connect to MySQL database
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("✅ Connected to MySQL database: " + URL);
        return conn;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("🎯 Connection test successful!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
