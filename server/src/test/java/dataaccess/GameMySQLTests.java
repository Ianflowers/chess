package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameMySQLTests {

    private static GameDAO gameDAO;
    private static Connection connection;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        connection = DatabaseManager.getConnection();
        gameDAO = new GameMySQL();

        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS game (" +
                    "gameID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "whiteUsername VARCHAR(50), " +
                    "blackUsername VARCHAR(50), " +
                    "gameName VARCHAR(100) UNIQUE NOT NULL, " +
                    "game TEXT)");
        }
    }

    @AfterAll
    static void tearDownAfterClass() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DROP TABLE IF EXISTS game");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        connection.close();
    }

    @BeforeEach
    void setUpBeforeEach() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    void insertGameDuplicateName() throws DataAccessException {
        GameData game1 = new GameData(0, null, null, "Duplicate Game", null);
        GameData game2 = new GameData(0, null, null, "Duplicate Game", null);
        gameDAO.insertGame(game1);

        assertThrows(ForbiddenException.class, () -> gameDAO.insertGame(game2));
    }

    @Test
    void getGameByIdNotFound() throws DataAccessException {
        Optional<GameData> fetched = gameDAO.getGameById(999);
        assertFalse(fetched.isPresent());
    }

    @Test
    void getAllGames() throws DataAccessException {
        GameData game1 = new GameData(0, null, null, "Game1", null);
        GameData game2 = new GameData(0, null, null, "Game2", null);
        gameDAO.insertGame(game1);
        gameDAO.insertGame(game2);

        List<GameData> games = gameDAO.getAllGames();
        assertEquals(2, games.size());
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Update Game", null);
        int gameID = gameDAO.insertGame(game);
        GameData updated = new GameData(gameID, "Alice", "Bob", "Update Game", null);
        gameDAO.updateGame(updated);
        Optional<GameData> fetched = gameDAO.getGameById(gameID);

        assertTrue(fetched.isPresent());
        assertEquals("Alice", fetched.get().whiteUsername());
        assertEquals("Bob", fetched.get().blackUsername());
    }

    @Test
    void updateGameNotFound() {
        GameData game = new GameData(999, "Alice", "Bob", "Nonexistent", null);
        assertThrows(BadRequestException.class, () -> gameDAO.updateGame(game));
    }

    @Test
    void clearTable() throws DataAccessException {
        GameData game = new GameData(0, null, null, "Clear Me", null);
        gameDAO.insertGame(game);
        gameDAO.clear();
        List<GameData> games = gameDAO.getAllGames();

        assertTrue(games.isEmpty());
    }
}
