package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dataaccess.BadRequestException;
import dataaccess.ForbiddenException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Handler;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.ErrorResult;
import result.GetAllGamesResult;
import result.JoinGameResult;
import service.GameService;

public class GameHandler {

    public final Handler listGames;
    public final Handler createGame;
    public final Handler joinGame;

    public GameHandler(GameService gameService, Gson gson) {

        // List Games - GET /game
        listGames = ctx -> {
            String authHeader = ctx.header("Authorization");
            if (authHeader == null) {
                ctx.status(401).json(new ErrorResult("error: unauthorized"));
                return;
            }

            String authToken;
            if (authHeader.startsWith("Bearer ")) {
                authToken = authHeader.substring("Bearer ".length());
            } else {
                authToken = authHeader;
            }

            try {
                GetAllGamesResult result = gameService.getAllGames(authToken);
                ctx.status(200).json(result);
            } catch (Exception e) {
                ctx.status(500).json(new ErrorResult("error: " + e.getMessage()));
            }
        };

        // Create Game - POST /game
        createGame = ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.isBlank()) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
                return;
            }
            try {
                CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
                CreateGameResult result = gameService.createGame(request, authToken);
                Gson test = new GsonBuilder().serializeNulls().create();
                String jsonResult = test.toJson(result);
                ctx.status(200).json(result);

            } catch (BadRequestException e) {
                ctx.status(400).json(new ErrorResult("Error: bad request"));
            } catch (UnauthorizedException e) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
            } catch (Exception e) {
                ctx.status(500).json(new ErrorResult("Error: " + e.getMessage()));
            }
        };

        // Join Game - PUT /game
        joinGame = ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.isBlank()) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
                return;
            }
            try {
                JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
                JoinGameResult result = gameService.joinGame(request, authToken);
                ctx.status(200).json(result);
            } catch (JsonSyntaxException | BadRequestException e) {
                ctx.status(400).json(new ErrorResult("Error: bad request"));
            } catch (UnauthorizedException e) {
                ctx.status(401).json(new ErrorResult("Error: unauthorized"));
            } catch (ForbiddenException e) {
                ctx.status(403).json(new ErrorResult("Error: already taken"));
            } catch (Exception e) {
                ctx.status(500).json(new ErrorResult("Error: " + e.getMessage()));
            }
        };

    }
}
