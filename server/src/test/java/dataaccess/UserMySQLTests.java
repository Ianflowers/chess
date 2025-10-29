package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserMySQLTests {

    private static UserDAO userDAO;
    private static Connection connection;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        connection = DatabaseManager.getConnection();
        userDAO = new UserMySQL();

        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "PRIMARY KEY (username))");
        }
    }

    @AfterAll
    static void tearDownAfterClass() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        connection.close();
    }

    @BeforeEach
    void setUpBeforeEach() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM users");
        }
    }

    @Test
    void insertUserSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.insertUser(user);
        Optional<UserData> fetchedUser = userDAO.getUserByUsername("testUser");

        assertTrue(fetchedUser.isPresent());
        assertEquals("testUser", fetchedUser.get().username());
        assertEquals("test@example.com", fetchedUser.get().email());
    }

    @Test
    void insertUserDuplicateUsername() {
        UserData user1 = new UserData("testUser", "password123", "test@example.com");
        UserData user2 = new UserData("testUser", "password456", "test2@example.com");

        try {
            userDAO.insertUser(user1);
            userDAO.insertUser(user2);
            fail("Expected ForbiddenException");
        } catch (ForbiddenException e) {
            assertEquals("Username already exists", e.getMessage());
        } catch (Exception e) {
            fail("Expected ForbiddenException, but got " + e.getClass().getSimpleName());
        }
    }

    @Test
    void getUserByUsernameSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.insertUser(user);
        Optional<UserData> fetchedUser = userDAO.getUserByUsername("testUser");

        assertTrue(fetchedUser.isPresent());
        assertEquals("testUser", fetchedUser.get().username());
        assertEquals("test@example.com", fetchedUser.get().email());
    }

    @Test
    void getUserByUsernameUserNotFound() throws DataAccessException {
        Optional<UserData> fetchedUser = userDAO.getUserByUsername("nonExistentUser");

        assertFalse(fetchedUser.isPresent());
    }

    @Test
    void clearSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.insertUser(user);
        userDAO.clear();
        Optional<UserData> fetchedUser = userDAO.getUserByUsername("testUser");

        assertFalse(fetchedUser.isPresent());
    }

}
