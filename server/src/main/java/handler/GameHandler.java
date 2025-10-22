package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Handler;
import request.*;
import result.*;
import service.GameService;

public class GameHandler {

    public final Handler listGames;

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



    }

}
