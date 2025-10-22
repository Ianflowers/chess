package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Handler;
import request.*;
import result.*;
import service.GameService;

public class GameHandler {

    public final Handler listGames;
    public final Handler createGame;


    public GameHandler(GameService gameService, Gson gson) {

        // List Games - GET /game
            this.listGames = ctx -> {
                try {
                    String authToken = ctx.header("Authorization");
                    if (authToken == null || authToken.isEmpty()) {
                        ctx.status(401).result("Missing Authorization header");
                        return;
                    }

                    GetAllGamesResult result = gameService.getAllGames(authToken);
                    ctx.status(200).json(result);
                } catch (DataAccessException e) {
                    ctx.status(401).result("Unauthorized: " + e.getMessage());
                } catch (Exception e) {
                    ctx.status(500).result("Error: " + e.getMessage());
                }
            };

        // Create Game - POST /game
        this.createGame = ctx -> {
            try {
                String authToken = ctx.header("Authorization");
                if (authToken == null || authToken.isEmpty()) {
                    ctx.status(401).result("Missing Authorization header");
                    return;
                }

                CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
                if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                    ctx.status(400).result("Missing or empty gameName in request body");
                    return;
                }

                CreateGameResult result = gameService.createGame(request, authToken);
                ctx.status(201).json(result);

            } catch (DataAccessException e) {
                ctx.status(401).result("Unauthorized: " + e.getMessage());
            } catch (Exception e) {
                ctx.status(500).result("Error: " + e.getMessage());
            }
        };







    }

}
