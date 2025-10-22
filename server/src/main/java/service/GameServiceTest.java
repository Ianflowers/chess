package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import result.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() throws DataAccessException {
        AuthMemory authDAO = new AuthMemory();
        GameMemory gameDAO = new GameMemory();

        gameService = new GameService(gameDAO, authDAO);

        String username = "tester";
        String token = "valid-token";

        authDAO.insertAuth(new AuthData(token, username));
        gameDAO.insertGame(new GameData(1, "", "", "Test Game 1", null));
        gameDAO.insertGame(new GameData(2, "", "", "Test Game 2", null));
    }

    @Test
    void getAllGames_success() throws DataAccessException {
        String token = "valid-token";

        GetAllGamesResult result = gameService.getAllGames(token);

        assertNotNull(result);
        assertEquals("Retrieved all games successfully", result.message());
        assertEquals(2, result.games().size());
    }

    @Test
    void getAllGames_missingToken_throwsException() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.getAllGames(null));
        assertEquals("Invalid or missing auth token", exception.getMessage());
    }

    @Test
    void getAllGames_invalidToken_throwsException() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.getAllGames("invalid-token"));
        assertEquals("Invalid or missing auth token", exception.getMessage());
    }



}
