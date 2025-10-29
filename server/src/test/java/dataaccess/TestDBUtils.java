package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDBUtils {

    public static void clearTables(Connection connection, String... tables) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            for (String table : tables) {
                stmt.execute("DELETE FROM " + table);
            }
        }
    }

    public static void insertTestUser(Connection connection, String username, String password, String email) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.executeUpdate();
        }
    }

}
