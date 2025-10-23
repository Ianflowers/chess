package service;

import dataaccess.*;
import model.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import result.ClearResult;

import java.util.Optional;

public class ClearServiceTest {

    private ClearService clearService;
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserMemory();
        gameDAO = new GameMemory();
        authDAO = new AuthMemory();
        clearService = new ClearService(userDAO, gameDAO, authDAO);
    }

    @Test
    void clearAllsuccess() throws DataAccessException {
        userDAO.insertUser(new UserData("testuser", "password", "email@test.com"));
        gameDAO.insertGame(new GameData(1, "testuser", "otheruser", "game", null));
        authDAO.insertAuth(new AuthData("testuser", "testuser"));

        ClearResult result = clearService.clearAll();

        assertEquals("Clear succeeded.", result.message());
        assertTrue(userDAO.getUserByUsername("testuser").isEmpty(), "Users should be cleared");
        assertTrue(gameDAO.getAllGames().isEmpty(), "Games should be cleared");
        assertTrue(authDAO.getAuthByToken("somerandomtoken").isEmpty(), "Auth should be cleared");
    }

    @Test
    void clearAllUserDAOFailsThrowsException() {
        UserDAO badUserDAO = new UserDAO() {
            public void insertUser(UserData user) {}
            public Optional<UserData> getUserByUsername(String username) { return Optional.empty(); }
            public void clear() throws DataAccessException {
                throw new DataAccessException("Database issue occurred during clear operation.");
            }
        };

        ClearService failingService = new ClearService(badUserDAO, gameDAO, authDAO);
        DataAccessException ex = assertThrows(DataAccessException.class, failingService::clearAll);
        assertEquals("Database issue occurred during clear operation.", ex.getMessage());
    }


}
