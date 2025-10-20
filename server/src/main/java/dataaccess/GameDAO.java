package dataaccess;

import model.GameData;
import java.util.List;
import java.util.Optional;

public interface GameDAO {

    void insertGame(GameData game) throws DataAccessException;

    Optional<GameData> getGameById(String gameId) throws DataAccessException;

    List<GameData> getAllGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    void deleteGame(String gameId) throws DataAccessException;
}
