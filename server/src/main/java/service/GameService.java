package service;

import dataaccess.*;
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

    public GetAllGamesResult getAllGames(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException();
        }
        Optional<AuthData> authOpt = authDAO.getAuthByToken(authToken);
        if (authOpt.isEmpty()) {
            throw new UnauthorizedException();
        }

        List<GameData> games = gameDAO.getAllGames();
        return new GetAllGamesResult("Retrieved all games successfully", games);
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws DataAccessException {
        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException();
        }

        Optional<AuthData> authOpt = authDAO.getAuthByToken(authToken);
        if (authOpt.isEmpty()) {
            throw new UnauthorizedException();
        }

        int gameID = generateGameID();
        GameData game = new GameData(gameID, null, null, request.gameName(), null);
        gameDAO.insertGame(game);

        return new CreateGameResult("Game created successfully", game);
    }

    public JoinGameResult joinGame(JoinGameRequest request, String authToken) throws DataAccessException {
        if (request == null || request.gameID() == null || request.playerColor() == null || request.playerColor().trim().isEmpty()) {
            throw new BadRequestException();
        }

        Optional<AuthData> authOpt = authDAO.getAuthByToken(authToken);
        if (authOpt.isEmpty()) {
            throw new UnauthorizedException();
        }
        String joiningUsername = authOpt.get().username();

        Optional<GameData> gameOpt = gameDAO.getGameById(request.gameID());
        if (gameOpt.isEmpty()) {
            throw new BadRequestException();
        }
        GameData game = gameOpt.get();

        String requestedColor = request.playerColor().trim().toLowerCase();
        if (!requestedColor.equals("white") && !requestedColor.equals("black")) {
            throw new BadRequestException();
        }

        String newWhite = game.whiteUsername();
        String newBlack = game.blackUsername();

        if (requestedColor.equals("white")) {
            if (newWhite != null && !newWhite.isBlank()) {
                throw new ForbiddenException();
            }
            newWhite = joiningUsername;
        } else {
            if (newBlack != null && !newBlack.isBlank()) {
                throw new ForbiddenException();
            }
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

        return new JoinGameResult("Player joined the game successfully", updatedGame);
    }

    private int generateGameID() { return counter.getAndIncrement(); }

}
