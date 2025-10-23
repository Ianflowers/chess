package service;

import dataaccess.*;
import model.*;
import request.*;
import result.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

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
        gameDAO.insertGame(new GameData(2, "", "", "Test Game 1", null));
        gameDAO.insertGame(new GameData(3, "", "", "Test Game 2", null));
    }

    // Get Games Tests
    @Test
    void getAllGamesSuccess() throws DataAccessException {
        String token = "valid-token";
        CreateGameRequest request = new CreateGameRequest("New Chess Game");
        CreateGameResult result = gameService.createGame(request, token);

        assertNotNull(result);
    }



    @Test
    void getAllGamesMissingTokenThrowsUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.getAllGames(""));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void getAllGamesInvalidTokenThrowsUnauthorizedException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.getAllGames("invalid-token"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // Create Game Tests
    @Test
    void createGameSuccess() throws DataAccessException {
        String token = "valid-token";
        CreateGameRequest request = new CreateGameRequest("New Chess Game");
        CreateGameResult result = gameService.createGame(request, token);

        assertNotNull(result);
    }

    @Test
    void createGameMissingGameNameThrowsBadRequestException() {
        String token = "valid-token";
        CreateGameRequest request = new CreateGameRequest(null);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.createGame(request, token));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void createGameMissingAuthTokenThrowsUnauthorizedException() {
        CreateGameRequest request = new CreateGameRequest("New Chess Game");
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.createGame(request, null));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void createGameInvalidAuthTokenThrowsUnauthorizedException() {
        CreateGameRequest request = new CreateGameRequest("New Chess Game");
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.createGame(request, "bad-token"));
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // Join Game Tests
    @Test
    void joinGameSuccessWhite() throws DataAccessException {
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
    void joinGameSuccessBlack() throws DataAccessException {
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
    void joinGameMissingGameIdThrowsBadRequestException() {
        String token = "valid-token";
        JoinGameRequest request = new JoinGameRequest(null, "white");
        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.joinGame(request, token));

        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void joinGameMissingPlayerColorThrowsBadRequestException() {
        String token = "valid-token";
        JoinGameRequest request = new JoinGameRequest(1, null);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.joinGame(request, token));

        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void joinGameInvalidPlayerColorThrowsBadRequestException() throws DataAccessException {
        String token = "valid-token";
        GameData game = gameService.getAllGames(token).games().getFirst();
        JoinGameRequest joinRequest = new JoinGameRequest(game.gameID(), "green");
        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.joinGame(joinRequest, token));

        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void joinGameMissingAuthTokenThrowsUnauthorizedException() {
        JoinGameRequest request = new JoinGameRequest(1, "white");
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.joinGame(request, null));

        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void joinGameInvalidAuthTokenThrowsUnauthorizedException() {
        JoinGameRequest request = new JoinGameRequest(1, "white");
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> gameService.joinGame(request, "bad-token"));

        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void joinGameGameNotFoundThrowsBadRequestException() {
        String token = "valid-token";
        JoinGameRequest request = new JoinGameRequest(999, "white");
        BadRequestException exception = assertThrows(BadRequestException.class, () -> gameService.joinGame(request, token));

        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void joinGamePlayerAlreadyJoinedThrowsForbiddenException() throws DataAccessException {
        String token = "valid-token";
        GameData game = gameService.getAllGames(token).games().getFirst();
        JoinGameRequest joinRequest = new JoinGameRequest(game.gameID(), "white");
        gameService.joinGame(joinRequest, token);
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> gameService.joinGame(joinRequest, token));
        assertEquals("Error: already taken", exception.getMessage());
    }
}
