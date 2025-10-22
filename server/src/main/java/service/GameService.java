package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import request.*;
import result.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final AtomicInteger counter = new AtomicInteger(1);

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public GetAllGamesResult getAllGames() throws DataAccessException {
        List<GameData> games = gameDAO.getAllGames();
        return new GetAllGamesResult("Retrieved all games successfully", games);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        if (request == null || request.whiteUsername() == null || request.blackUsername() == null) {
            throw new DataAccessException("Invalid request data");
        }

        int gameID = generateGameID();
        GameData game = new GameData(gameID, request.whiteUsername(), request.blackUsername(), request.gameName(), null);
        gameDAO.insertGame(game);

        return new CreateGameResult("Game created successfully", game);
    }

    public UpdateGameResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        if (request == null) { throw new DataAccessException("Missing join game request"); }
        if (request.gameId() == null || request.gameId().trim().isEmpty()) { throw new DataAccessException("Missing or empty game ID"); }
        if (request.playerColor() == null || request.playerColor().trim().isEmpty()) { throw new DataAccessException("Missing player color"); }

        Optional<AuthData> authOpt = authDAO.getAuthByToken(authToken);
        if (authOpt.isEmpty()) { throw new DataAccessException("Invalid or expired auth token"); }
        String joiningUsername = authOpt.get().username();

        Optional<GameData> gameOpt = gameDAO.getGameById(request.gameId());
        if (gameOpt.isEmpty()) { throw new DataAccessException("Game with ID " + request.gameId() + " not found"); }

        GameData game = gameOpt.get();

        String requestedColor = request.playerColor().trim().toLowerCase();
        if (!requestedColor.equals("white") && !requestedColor.equals("black")) {
            throw new DataAccessException("Invalid player color: must be 'white' or 'black'");
        }

        if (joiningUsername.equals(game.whiteUsername()) || joiningUsername.equals(game.blackUsername())) {
            throw new DataAccessException("Player already joined the game");
        }

        String newWhite = game.whiteUsername();
        String newBlack = game.blackUsername();

        if (requestedColor.equals("white")) {
            if (newWhite != null && !newWhite.isEmpty()) { throw new DataAccessException("White player already assigned"); }
            newWhite = joiningUsername;
        } else {
            if (newBlack != null && !newBlack.isEmpty()) { throw new DataAccessException("Black player already assigned"); }
            newBlack = joiningUsername;
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                newWhite,
                newBlack,
                game.gameName(),
                game.game()
        );

        gameDAO.updateGame(updatedGame);
        return new UpdateGameResult("Joined game successfully", updatedGame);
    }

    private int generateGameID() {
        return counter.getAndIncrement();
    }

}
