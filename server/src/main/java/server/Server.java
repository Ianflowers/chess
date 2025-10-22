package server;

import com.google.gson.Gson;
import io.javalin.*;

import dataaccess.*;
import request.*;
import result.*;
import service.*;
import handler.*;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

    // DAOs
    private final UserDAO userDAO = new UserMemory();
    private final AuthDAO authDAO = new AuthMemory();
    private final GameDAO gameDAO = new GameMemory();

    // Services
    private final UserService userService = new UserService(userDAO);
    private final AuthService authService = new AuthService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);
    private final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        registerEndpoints();
        registerExceptionHandlers();
    }

    private void registerEndpoints() {

        // User Registration - POST /user
        javalin.post("/user", ctx -> {
            var request = gson.fromJson(ctx.body(), UserRequest.class);
            var result = userService.registerUser(request);
            ctx.json(result);
        });

        // User Login - POST /session
        javalin.post("/session", ctx -> {
            var request = gson.fromJson(ctx.body(), LoginRequest.class);
            var result = authService.login(request);
            ctx.json(result);
        });

        // User Logout - DELETE /session
        javalin.delete("/session", ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401).result("Missing Authorization header");
                return;
            }

            try {
                LogoutRequest request = new LogoutRequest(authToken);
                LogoutResult result = authService.logout(request);
                ctx.status(200).json(result);

            } catch (DataAccessException e) {
                ctx.status(401).result("Unauthorized: " + e.getMessage());
            }
        });

        // List Games - GET /game
        javalin.get("/game", ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401).result("Missing Authorization header");
                return;
            }

            try {
                var games = gameService.getAllGames();
                ctx.json(games);
            } catch (DataAccessException e) {
                ctx.status(500).result("Internal server error: " + e.getMessage());
            }
        });

        // Create Game - POST /game
        javalin.post("/game", ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401).result("Missing Authorization header");
                return;
            }
            var request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            var result = gameService.createGame(request);
            ctx.json(result);
        });

        // Join Game - PUT /game
        javalin.put("/game", ctx -> {
            String authToken = ctx.header("Authorization");
            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401).result("Missing Authorization header");
                return;
            }
            var request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            var result = gameService.joinGame(request, authToken);
            ctx.json(result);
        });

        // Clear DB - DELETE /db
        javalin.delete("/db", ctx -> {
            var result = clearService.clearAll();
            ctx.json(result);
        });
    }

    private void registerExceptionHandlers() {
        javalin.exception(DataAccessException.class, (e, ctx) -> ctx.status(500).json(new ErrorResult(e.getMessage())));
        javalin.exception(Exception.class, (e, ctx) -> ctx.status(500).json(new ErrorResult("Internal Server Error")));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

}
