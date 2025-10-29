package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthMySQLTests {

    private static AuthDAO authDAO;
    private static Connection connection;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        connection = DatabaseManager.getConnection();
        authDAO = new AuthMySQL();

        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) NOT NULL PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL)");
        }

        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS auth (" +
                    "authToken VARCHAR(255) NOT NULL, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "PRIMARY KEY (authToken), " +
                    "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE)");
        }
    }

    @AfterAll
    static void tearDownAfterClass() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DROP TABLE IF EXISTS auth");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        connection.close();
    }

    @BeforeEach
    void setUpBeforeEach() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM auth");
            stmt.execute("DELETE FROM users");
        }

        try (var stmt = connection.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
            stmt.setString(1, "testUser");
            stmt.setString(2, "password123");
            stmt.setString(3, "test@example.com");
            stmt.executeUpdate();
        }
    }

    @Test
    void insertAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("authToken123", "testUser");
        authDAO.insertAuth(auth);

        Optional<AuthData> fetchedAuth = authDAO.getAuthByToken("authToken123");
        assertTrue(fetchedAuth.isPresent());
        assertEquals("authToken123", fetchedAuth.get().authToken());
        assertEquals("testUser", fetchedAuth.get().username());
    }

    @Test
    void insertAuthDuplicateToken() {
        AuthData auth1 = new AuthData("authToken123", "testUser");
        AuthData auth2 = new AuthData("authToken123", "testUser");

        try {
            authDAO.insertAuth(auth1);
            authDAO.insertAuth(auth2);
            fail("Expected ForbiddenException");
        } catch (ForbiddenException e) {
            assertEquals("Auth token already exists or invalid username", e.getMessage());
        } catch (Exception e) {
            fail("Expected ForbiddenException, but got " + e.getClass().getSimpleName());
        }
    }

    @Test
    void getAuthByTokenSuccess() throws DataAccessException {
        AuthData auth = new AuthData("authToken123", "testUser");
        authDAO.insertAuth(auth);

        Optional<AuthData> fetchedAuth = authDAO.getAuthByToken("authToken123");
        assertTrue(fetchedAuth.isPresent());
        assertEquals("authToken123", fetchedAuth.get().authToken());
        assertEquals("testUser", fetchedAuth.get().username());
    }

    @Test
    void getAuthByTokenNotFound() throws DataAccessException {
        Optional<AuthData> fetchedAuth = authDAO.getAuthByToken("nonExistentToken");
        assertFalse(fetchedAuth.isPresent());
    }

    @Test
    void deleteAuthTokenSuccess() throws DataAccessException {
        AuthData auth = new AuthData("authToken123", "testUser");
        authDAO.insertAuth(auth);

        authDAO.deleteAuth("authToken123");
        Optional<AuthData> fetchedAuth = authDAO.getAuthByToken("authToken123");

        assertFalse(fetchedAuth.isPresent());
    }

    @Test
    void deleteAuthTokenNotFound() {
        try {
            authDAO.deleteAuth("nonExistentToken");
            fail("Expected UnauthorizedException");
        } catch (UnauthorizedException e) {
            assertEquals("Auth token not found", e.getMessage());
        } catch (Exception e) {
            fail("Expected UnauthorizedException, but got " + e.getClass().getSimpleName());
        }
    }

    @Test
    void clearAuthTableSuccess() throws DataAccessException {
        AuthData auth1 = new AuthData("testToken123", "testUser");
        AuthData auth2 = new AuthData("testToken456", "testUser");

        authDAO.insertAuth(auth1);
        authDAO.insertAuth(auth2);

        authDAO.clear();

        assertFalse(authDAO.getAuthByToken("testToken123").isPresent());
        assertFalse(authDAO.getAuthByToken("testToken456").isPresent());
    }

}
