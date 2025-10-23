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
//        gameDAO.insertGame(new GameData(gameService.generateGameID(), "", "", "Test Game 1", null));
//        gameDAO.insertGame(new GameData(gameService.generateGameID(), "", "", "Test Game 2", null));
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
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.getAllGames(""));
        assertEquals("Invalid or missing auth token", exception.getMessage());
    }

    @Test
    void getAllGames_invalidToken_throwsException() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.getAllGames("invalid-token"));
        assertEquals("Invalid or missing auth token", exception.getMessage());
    }

    // Create Game Tests
//    @Test
//    void createGame_success() throws DataAccessException {
//        String token = "valid-token";
//        CreateGameRequest request = new CreateGameRequest(null, null, "New Chess Game", null);
//        CreateGameResult result = gameService.createGame(request, token);
//
//        assertNotNull(result);
//        assertEquals("Game created successfully", result.message());
//        assertNotNull(result.game());
//        assertEquals("New Chess Game", result.game().gameName());
//        assertEquals("tester", result.game().whiteUsername());
//        assertNull(result.game().blackUsername());
//    }
//
//    @Test
//    void createGame_missingGameName_throwsException() {
//        String token = "valid-token";
//        CreateGameRequest request = new CreateGameRequest(null, null, null, null);
//        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.createGame(request, token));
//        assertEquals("Invalid request data: missing game name", exception.getMessage());
//    }
//
//    @Test
//    void createGame_missingAuthToken_throwsException() {
//        CreateGameRequest request = new CreateGameRequest(null, null, "New Chess Game", null);
//        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.createGame(request, null));
//        assertEquals("Invalid or expired auth token", exception.getMessage());
//    }
//
//    @Test
//    void createGame_invalidAuthToken_throwsException() {
//        CreateGameRequest request = new CreateGameRequest(null, null, "New Chess Game", null);
//        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.createGame(request, "bad-token"));
//        assertEquals("Invalid or expired auth token", exception.getMessage());
//    }

    // Join Game Tests
    @Test
    void joinGame_success_white() throws DataAccessException {
        String token = "valid-token";
        GameData game = gameService.getAllGames(token).games().getFirst();
        JoinGameRequest joinRequest = new JoinGameRequest(game.gameID(), "white");
        JoinGameResult joinResult = gameService.joinGame(joinRequest, token);

        assertNotNull(joinResult);
        assertEquals("Player joined the game successfully", joinResult.message());
        assertEquals("tester", joinResult.game().whiteUsername());
        assertTrue(joinResult.game().blackUsername() == null || joinResult.game().blackUsername().isEmpty());
    }

    @Test
    void joinGame_success_black() throws DataAccessException {
        String token = "valid-token";
        GameData game = gameService.getAllGames(token).games().get(1);
        JoinGameRequest joinRequest = new JoinGameRequest(game.gameID(), "black");
        JoinGameResult joinResult = gameService.joinGame(joinRequest, token);

        assertNotNull(joinResult);
        assertEquals("Player joined the game successfully", joinResult.message());
        assertEquals("tester", joinResult.game().blackUsername());
        assertTrue(joinResult.game().whiteUsername() == null || joinResult.game().whiteUsername().isEmpty());
    }

    @Test
    void joinGame_missingGameId_throwsException() {
        String token = "valid-token";
        JoinGameRequest request = new JoinGameRequest(null, "white");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(request, token));

        assertEquals("Missing or empty game ID", exception.getMessage());
    }

    @Test
    void joinGame_missingPlayerColor_throwsException() {
        String token = "valid-token";
        JoinGameRequest request = new JoinGameRequest(1, null);
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(request, token));

        assertEquals("Missing player color", exception.getMessage());
    }

    @Test
    void joinGame_invalidPlayerColor_throwsException() throws DataAccessException {
        String token = "valid-token";
        GameData game = gameService.getAllGames(token).games().getFirst();
        JoinGameRequest joinRequest = new JoinGameRequest(game.gameID(), "green");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(joinRequest, token));

        assertEquals("Invalid player color: must be 'white' or 'black'", exception.getMessage());
    }

    @Test
    void joinGame_missingAuthToken_throwsException() {
        JoinGameRequest request = new JoinGameRequest(1, "white");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(request, null));

        assertEquals("Invalid or expired auth token", exception.getMessage());
    }

    @Test
    void joinGame_invalidAuthToken_throwsException() {
        JoinGameRequest request = new JoinGameRequest(1, "white");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(request, "bad-token"));

        assertEquals("Invalid or expired auth token", exception.getMessage());
    }

    @Test
    void joinGame_gameNotFound_throwsException() {
        String token = "valid-token";
        JoinGameRequest request = new JoinGameRequest(999, "white");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(request, token));

        assertEquals("Game with ID 999 not found", exception.getMessage());
    }

    @Test
    void joinGame_playerAlreadyJoined_throwsException() throws DataAccessException {
        String token = "valid-token";

        GameData game = gameService.getAllGames(token).games().getFirst();
        JoinGameRequest joinRequest = new JoinGameRequest(game.gameID(), "white");
        gameService.joinGame(joinRequest, token);

        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameService.joinGame(joinRequest, token));
        assertEquals("Player already joined the game", exception.getMessage());
    }
}
