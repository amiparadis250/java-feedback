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
    public static void createTables() {
        String enableExtension = "CREATE EXTENSION IF NOT EXISTS \"pgcrypto\";";
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
               + "id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  "
                + "email VARCHAR(100) UNIQUE NOT NULL, "
                + "names VARCHAR(50), "
                + "password VARCHAR(255) NOT NULL)"; 

        String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedbacks ("
                 + "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), "
                + "first_name VARCHAR(50) NOT NULL, "
                + "last_name VARCHAR(50) NOT NULL, "
                + "email VARCHAR(100) UNIQUE NOT NULL, "
                + "gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')), "
                + "comment TEXT NOT NULL CHECK (LENGTH(comment) BETWEEN 50 AND 100), "
                + "satisfaction VARCHAR(20) CHECK (satisfaction IN ('Very Satisfied', 'Satisfied', 'Neutral', 'Dissatisfied')), "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"; 

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(enableExtension);
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
