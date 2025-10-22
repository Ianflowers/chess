package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import request.*;
import result.*;

import static org.junit.jupiter.api.Assertions.*;

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
        gameDAO.insertGame(new GameData(gameService.generateGameID(), "", "", "Test Game 1", null));
        gameDAO.insertGame(new GameData(gameService.generateGameID(), "", "", "Test Game 2", null));
    }

    // Get Games Tests
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

    // Create Game Tests
    @Test
    void createGame_success() throws DataAccessException {
        String token = "valid-token";
        CreateGameRequest request = new CreateGameRequest(null, null, "New Chess Game", null);
        CreateGameResult result = gameService.createGame(request, token);

        assertNotNull(result);
        assertEquals("Game created successfully", result.message());
        assertNotNull(result.game());
        assertEquals("New Chess Game", result.game().gameName());
        assertEquals("tester", result.game().whiteUsername()); // Auth user is white player
        assertNull(result.game().blackUsername());
    }

    @Test
    void createGame_missingGameName_throwsException() {
        String token = "valid-token";
        CreateGameRequest request = new CreateGameRequest(null, null, null, null);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.createGame(request, token));

        assertEquals("Invalid request data: missing game name", exception.getMessage());
    }

    @Test
    void createGame_missingAuthToken_throwsException() {
        CreateGameRequest request = new CreateGameRequest(null, null, "New Chess Game", null);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.createGame(request, null));

        assertEquals("Invalid or expired auth token", exception.getMessage());
    }

    @Test
    void createGame_invalidAuthToken_throwsException() {
        CreateGameRequest request = new CreateGameRequest(null, null, "New Chess Game", null);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.createGame(request, "bad-token"));

        assertEquals("Invalid or expired auth token", exception.getMessage());
    }


}
