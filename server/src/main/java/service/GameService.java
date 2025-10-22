package service;

import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.GameData;
import request.CreateGameRequest;
import request.UpdateGameRequest;
import request.DeleteGameRequest;
import request.GetGameRequest;
import result.CreateGameResult;
import result.UpdateGameResult;
import result.DeleteGameResult;
import result.GetGameResult;

import java.util.Optional;
import java.util.UUID;

public class GameService {

    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) { this.gameDAO = gameDAO; }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        if (request == null || request.whiteUsername() == null || request.blackUsername() == null) {
            throw new DataAccessException("Invalid request data");
        }

        int gameID;
        GameData existingGame;

        do {
            gameID = generateGameID();
            existingGame = gameDAO.getGameById(String.valueOf(gameID)).orElse(null);
        } while (existingGame != null);

        GameData game = new GameData(gameID, request.whiteUsername(), request.blackUsername(), request.gameName(), null);
        gameDAO.insertGame(game);

        return new CreateGameResult("Game created successfully", game);
    }

    public UpdateGameResult updateGame(UpdateGameRequest request) throws DataAccessException {
        if (request == null || request.gameId() == null) {
            throw new DataAccessException("Invalid request data");
        }

        Optional<GameData> gameOpt = gameDAO.getGameById(request.gameId());

        if (gameOpt.isEmpty()) {
            throw new DataAccessException("Game with ID " + request.gameId() + " not found");
        }

        GameData game = gameOpt.get();
        GameData updatedGame = new GameData(
                game.gameID(),
                request.whiteUsername(),
                request.blackUsername(),
                request.gameName(),
                game.game()
        );

        gameDAO.updateGame(updatedGame);
        return new UpdateGameResult("Game updated successfully", updatedGame);
    }

    public DeleteGameResult deleteGame(DeleteGameRequest request) throws DataAccessException {
        if (request == null || request.gameId() == null) {
            throw new DataAccessException("Invalid request data");
        }

        Optional<GameData> gameOpt = gameDAO.getGameById(request.gameId());

        if (gameOpt.isEmpty()) {
            throw new DataAccessException("Game with ID " + request.gameId() + " not found");
        }

        gameDAO.deleteGame(request.gameId());
        return new DeleteGameResult("Game deleted successfully");
    }

    public GetGameResult getGame(GetGameRequest request) throws DataAccessException {
        if (request == null || request.gameId() == null) {
            throw new DataAccessException("Invalid request data");
        }

        Optional<GameData> gameOpt = gameDAO.getGameById(request.gameId());

        if (gameOpt.isEmpty()) {
            return new GetGameResult("Game not found");
        }

        return new GetGameResult("Game found successfully", gameOpt.get());
    }

    private int generateGameID() {
        UUID uuid = UUID.randomUUID();
        long mostSigBits = uuid.getMostSignificantBits();
        return (int) (mostSigBits & 0xFFFFFFFFL);
    }

}
