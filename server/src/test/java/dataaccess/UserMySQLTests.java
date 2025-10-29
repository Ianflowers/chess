package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
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
                    "username VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL)");
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
        TestDBUtils.clearTables(connection, "users");
        TestDBUtils.insertTestUser(connection, "testUser", "password123", "test@example.com");
    }

    @Test
    void insertUserSuccess() throws DataAccessException {
        Optional<UserData> fetchedUser = userDAO.getUserByUsername("testUser");

        assertTrue(fetchedUser.isPresent());
        assertEquals("testUser", fetchedUser.get().username());
        assertEquals("test@example.com", fetchedUser.get().email());
    }

    @Test
    void insertUserDuplicateUsername() {
        assertThrows(ForbiddenException.class, () -> {
            userDAO.insertUser(new UserData("testUser", "password123", "test@example.com"));
            userDAO.insertUser(new UserData("testUser", "password456", "test2@example.com"));
        });
    }

    @Test
    void getUserByUsernameSuccess() throws DataAccessException {
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
        userDAO.clear();
        assertFalse(userDAO.getUserByUsername("testUser").isPresent());
    }

}
