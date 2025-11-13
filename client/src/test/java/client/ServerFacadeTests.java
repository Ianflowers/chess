package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws Exception {
        facade.clear();
    }

    // Register Tests
    @Test
    public void registerSuccess() throws Exception {
        AuthData auth = facade.register("alice", "password", "alice@email.com");
        assertNotNull(auth);
        assertTrue(auth.authToken().length() > 5);
        assertEquals("alice", auth.username());
    }

    @Test
    public void registerFailDuplicateUser() throws Exception {
        facade.register("bob", "password", "bob@email.com");
        assertThrows(IOException.class, () -> facade.register("bob", "password", "bob@email.com"));
    }

    // Login Tests
    @Test
    public void loginSuccess() throws Exception {
        facade.register("charlie", "pass123", "c@email.com");
        AuthData auth = facade.login("charlie", "pass123");
        assertEquals("charlie", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void loginFailWrongPassword() throws Exception {
        facade.register("dave", "pass123", "d@email.com");
        assertThrows(IOException.class, () -> facade.login("dave", "wrongpass"));
    }

    // Logout Tests
    @Test
    public void logoutSuccess() throws Exception {
        AuthData auth = facade.register("eve", "pass", "e@email.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutFailInvalidToken() {
        assertThrows(IOException.class, () -> facade.logout("invalid-token"));
    }

    // Create Game Tests
    @Test
    public void createGameSuccess() throws Exception {
        AuthData auth = facade.register("frank", "pass", "f@email.com");
        assertDoesNotThrow(() -> facade.createGame("CoolGame", auth.authToken()));
    }

    @Test
    public void createGameFailInvalidAuth() {
        assertThrows(IOException.class, () -> facade.createGame("NoAuthGame", "badtoken"));
    }

    // List Games Tests

    @Test
    public void listGamesSuccess() throws Exception {
        AuthData auth = facade.register("george", "pass", "g@email.com");
        facade.createGame("GameOne", auth.authToken());
        facade.createGame("GameTwo", auth.authToken());

        List<GameData> games = facade.listGames(auth.authToken());
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesFailInvalidAuth() {
        assertThrows(IOException.class, () -> facade.listGames("bad-token"));
    }

    // Join Game Tests
    @Test
    public void joinGameSuccess() throws Exception {
        AuthData auth = facade.register("henry", "pass", "h@email.com");
        facade.createGame("TestJoin", auth.authToken());
        List<GameData> games = facade.listGames(auth.authToken());
        var game = games.getFirst();

        assertDoesNotThrow(() -> facade.joinGame(game.gameID(), "WHITE", auth.authToken()));
    }

    @Test
    public void joinGameFailInvalidGameID() throws Exception {
        AuthData auth = facade.register("irene", "pass", "i@email.com");
        assertThrows(IOException.class, () -> facade.joinGame(99999, "WHITE", auth.authToken()));
    }

}
