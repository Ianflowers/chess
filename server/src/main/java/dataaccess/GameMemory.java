package dataaccess;

import model.GameData;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;

public class GameMemory implements GameDAO {

    private final HashMap<Integer, GameData> gamesStore = new HashMap<>();

    @Override
    public int insertGame(GameData game) throws DataAccessException {
        if (game == null) { throw new BadRequestException(); }
        if (gamesStore.containsKey(game.gameID())) { throw new ForbiddenException(); }
        gamesStore.put(game.gameID(), game);
        return game.gameID();
    }

    @Override
    public Optional<GameData> getGameById(Integer gameId) throws DataAccessException {
        if (gameId == null) { throw new BadRequestException(); }
        return Optional.ofNullable(gamesStore.get(gameId));
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        return new ArrayList<>(gamesStore.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) { throw new BadRequestException();}
        if (!gamesStore.containsKey(game.gameID())) { throw new BadRequestException(); }
        gamesStore.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException { gamesStore.clear(); }

}
