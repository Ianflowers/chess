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
    private final UserDAO userDAO = new UserMySQL();
    private final AuthDAO authDAO = new AuthMySQL();
    private final GameDAO gameDAO = new GameMySQL();

    // Services
    private final UserService userService = new UserService(userDAO, authDAO);
    private final AuthService authService = new AuthService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);
    private final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

    // Handlers
    private final UserHandler userHandler = new UserHandler(userService, gson);
    private final AuthHandler authHandler = new AuthHandler(authService, gson);
    private final GameHandler gameHandler = new GameHandler(gameService, gson);
    private final ClearHandler clearHandler = new ClearHandler(clearService, gson);

    public Server() {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new io.javalin.json.JavalinGson());
        });

        registerEndpoints();
        registerExceptionHandlers();
    }

    private void registerEndpoints() {
        // HTTP Endpoints
        javalin.post("/user", userHandler.registerUser);    // User Registration - POST /user
        javalin.post("/session", authHandler.loginUser);    // User Login - POST /user
        javalin.delete("/session", authHandler.logoutUser); // User Logout - DELETE /session
        javalin.get("/game", gameHandler.listGames);        // List Games - GET /game
        javalin.post("/game", gameHandler.createGame);      // Create Game - POST /game
        javalin.put("/game", gameHandler.joinGame);         // Join Game - PUT /game
        javalin.delete("/db", clearHandler.clearAll);       // Clear DB - DELETE /db

        // Web Socket Endpoint
        WebSocketHandler wsHandler = new WebSocketHandler(gson, authDAO, gameDAO);
        javalin.ws("/ws", wsHandler::configureWs);
    }

    private void registerExceptionHandlers() {
        javalin.exception(BadRequestException.class, (e, ctx) -> ctx.status(400).json(new ErrorResult("Error: bad request")));
        javalin.exception(UnauthorizedException.class, (e, ctx) -> ctx.status(401).json(new ErrorResult("Error: unauthorized")));
        javalin.exception(ForbiddenException.class, (e, ctx) -> ctx.status(403).json(new ErrorResult("Error: already taken")));
        javalin.exception(DataAccessException.class, (e, ctx) -> ctx.status(500).json(new ErrorResult("Error: " + e.getMessage())));
        javalin.exception(Exception.class, (e, ctx) -> ctx.status(500).json(new ErrorResult("Error: internal server error")));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

}
