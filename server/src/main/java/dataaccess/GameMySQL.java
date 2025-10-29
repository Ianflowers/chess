package dataaccess;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.GameData;
import chess.ChessGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameMySQL implements GameDAO {

    private final Gson gson = new Gson();

    @Override
    public void insertGame(GameData game) throws DataAccessException {
        if (game == null) throw new BadRequestException();

        String sql = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, game.gameName());
            stmt.setString(5, gson.toJson(game.game()));

            stmt.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("Game with this ID already exists");
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting game", e);
        }
    }

    @Override
    public Optional<GameData> getGameById(Integer gameId) throws DataAccessException {
        if (gameId == null) throw new BadRequestException();

        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name = rs.getString("gameName");
                String jsonState = rs.getString("game");

                ChessGame game = null;
                if (jsonState != null) {
                    try {
                        game = gson.fromJson(jsonState, ChessGame.class);
                    } catch (JsonSyntaxException e) {
                        throw new DataAccessException("Invalid JSON in game", e);
                    }
                }

                return Optional.of(new GameData(id, white, black, name, game));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game", e);
        }
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";

        List<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("gameID");
                String white = rs.getString("whiteUsername");
                String black = rs.getString("blackUsername");
                String name = rs.getString("gameName");
                String jsonState = rs.getString("game");

                ChessGame game = null;
                if (jsonState != null) {
                    try {
                        game = gson.fromJson(jsonState, ChessGame.class);
                    } catch (JsonSyntaxException e) {
                        throw new DataAccessException("Invalid JSON in game", e);
                    }
                }

                games.add(new GameData(id, white, black, name, game));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all games", e);
        }

        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) throw new BadRequestException();

        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gson.toJson(game.game()));
            stmt.setInt(5, game.gameID());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new BadRequestException("Game does not exist");

        } catch (SQLException e) {
            throw new DataAccessException("Error updating game", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("TRUNCATE TABLE game");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException e) {
            throw new DataAccessException("Error clearing game table", e);
        }
    }

}
