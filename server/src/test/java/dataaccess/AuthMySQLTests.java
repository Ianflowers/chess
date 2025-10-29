package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
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
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        connection.close();
    }

    @BeforeEach
    void setUpBeforeEach() throws SQLException {
        TestDBUtils.clearTables(connection, "auth", "users");
        TestDBUtils.insertTestUser(connection, "testUser", "password123", "test@example.com");
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
        assertThrows(ForbiddenException.class, () -> {
            authDAO.insertAuth(new AuthData("authToken123", "testUser"));
            authDAO.insertAuth(new AuthData("authToken123", "testUser"));
        });
    }

    @Test
    void getAuthByTokenSuccess() throws DataAccessException {
        authDAO.insertAuth(new AuthData("authToken123", "testUser"));

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
        authDAO.insertAuth(new AuthData("authToken123", "testUser"));

        authDAO.deleteAuth("authToken123");
        assertFalse(authDAO.getAuthByToken("authToken123").isPresent());
    }

    @Test
    void deleteAuthTokenNotFound() {
        assertThrows(UnauthorizedException.class, () -> authDAO.deleteAuth("nonExistentToken"));
    }

    @Test
    void clearAuthTableSuccess() throws DataAccessException {
        authDAO.insertAuth(new AuthData("testToken123", "testUser"));
        authDAO.insertAuth(new AuthData("testToken456", "testUser"));

        authDAO.clear();

        assertFalse(authDAO.getAuthByToken("testToken123").isPresent());
        assertFalse(authDAO.getAuthByToken("testToken456").isPresent());
    }

}
