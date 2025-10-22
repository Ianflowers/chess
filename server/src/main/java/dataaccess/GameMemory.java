package dataaccess;

import model.GameData;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;

public class GameMemory implements GameDAO {

    private final HashMap<String, GameData> gamesStore = new HashMap<>();

    @Override
    public void insertGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game cannot be null");
        }
        String gameId = String.valueOf(game.gameID());
        if (gamesStore.containsKey(gameId)) {
            throw new DataAccessException("Game with ID " + gameId + " already exists");
        }
        gamesStore.put(gameId, game);
    }

    @Override
    public Optional<GameData> getGameById(String gameId) throws DataAccessException {
        if (gameId == null || gameId.isEmpty()) {
            throw new DataAccessException("Invalid game ID");
        }
        GameData game = gamesStore.get(gameId);
        return Optional.ofNullable(game);
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        return new ArrayList<>(gamesStore.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException("Game cannot be null");
        }
        String gameId = String.valueOf(game.gameID());
        if (!gamesStore.containsKey(gameId)) {
            throw new DataAccessException("Game with ID " + gameId + " does not exist");
        }
        gamesStore.put(gameId, game);
    }

    @Override
    public void deleteGame(String gameId) throws DataAccessException {
        if (gameId == null || gameId.isEmpty()) {
            throw new DataAccessException("Invalid game ID");
        }
        if (!gamesStore.containsKey(gameId)) {
            throw new DataAccessException("Game with ID " + gameId + " does not exist");
        }
        gamesStore.remove(gameId);
    }

    @Override
    public void clear() throws DataAccessException { gamesStore.clear(); }

}
