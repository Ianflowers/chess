package server;

import com.google.gson.Gson;
import io.javalin.*;

import dataaccess.*;
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

    // Handlers
    private final UserHandler userHandler = new UserHandler(userService, gson);
    private final AuthHandler authHandler = new AuthHandler(authService, gson);
    private final GameHandler gameHandler = new GameHandler(gameService, gson);
    private final ClearHandler clearHandler = new ClearHandler(clearService, gson);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        registerEndpoints();
        registerExceptionHandlers();
    }

    private void registerEndpoints() {

        // User Registration - POST /user
        javalin.post("/user", userHandler.registerUser);

        // User Login - POST /user
        javalin.post("/session", authHandler.loginUser);

        // User Logout - DELETE /session
        javalin.delete("/session", authHandler.logoutUser);

        // List Games - GET /game
        javalin.get("/game", gameHandler.listGames);

        // Create Game - POST /game
        javalin.post("/game", gameHandler.createGame);

        // Join Game - PUT /game
        javalin.put("/game", gameHandler.joinGame);

        // Clear DB - DELETE /db
        javalin.delete("/db", clearHandler.clearAll);
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
