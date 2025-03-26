
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/javalab"; 
    private static final String USER = "postgres"; 
    private static final String PASSWORD = "123"; 
    private static Connection connection = null;

    public static Connection connect() {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to database successfully!");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Database connection failed: " + e.getMessage());
            }
        }
        return connection;
    }

    // Create necessary tables
    public static void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id SERIAL PRIMARY KEY, "
                + "email VARCHAR(50) UNIQUE NOT NULL, "
                 + "names VARCHAR(50), "
                + "password VARCHAR(12) NOT NULL)";

        String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedbacks ("
                + "id SERIAL PRIMARY KEY, "
                + "firstname VARCHAR(50) NOT NULL, "
                + "lastname VARCHAR(50) NOT NULL, "
                + "email VARCHAR(100) UNIQUE NOT NULL, "
                + "gender VARCHAR(10) NOT NULL, "
                + "comment TEXT NOT NULL)";

        try (Statement stmt = connect().createStatement()) {
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createFeedbackTable);
            System.out.println("Tables created successfully!");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        connect();
        createTables(); 
    }
}
    

