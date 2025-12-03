package handler;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.*;
import model.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private final Gson gson;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    public WebSocketHandler(Gson gson, AuthDAO authDAO, GameDAO gameDAO) {
        this.gson = gson;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void configureWs(io.javalin.websocket.WsConfig ws) {
        ws.onConnect(this::onConnect);
        ws.onMessage(this::onMessage);
        ws.onClose(this::onClose);
        ws.onError(this::onError);
    }

    private void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Client connected: " + ctx.sessionId());
    }

    private void onMessage(WsMessageContext ctx) {
        String msg = ctx.message();

        try {
            UserGameCommand command = gson.fromJson(msg, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(ctx, command);
                case MAKE_MOVE -> handleMove(ctx, command);
                case LEAVE -> handleLeave(ctx, command);
                case RESIGN -> handleResign(ctx, command);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void onClose(WsCloseContext ctx) {
        System.out.println("Client disconnected: " + ctx.sessionId());
        gameConnections.forEach((gameID, clients) -> {
            if (clients.remove(ctx)) {
                broadcastNotification(gameID, "A player disconnected", ctx);
            }
        });
    }

    private void onError(WsErrorContext ctx) {
        System.out.println("WebSocket error: " + ctx.sessionId());
        ctx.error().printStackTrace();
    }

    private void handleConnect(WsContext ctx, UserGameCommand cmd) {
        String authToken = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();
        Optional<AuthData> authOpt;
        try {
            authOpt = authDAO.getAuthByToken(authToken);
        } catch (DataAccessException e) {
            sendError(ctx, "Server failure during auth lookup");
            return;
        }
        if (authOpt.isEmpty()) {
            sendError(ctx, "Invalid authToken");
            return;
        }

        String username = authOpt.get().username();
        Optional<GameData> gameOpt;
        try {
            gameOpt = gameDAO.getGameById(gameID);
        } catch (DataAccessException e) {
            sendError(ctx, "Server failure during game lookup");
            return;
        }
        if (gameOpt.isEmpty()) {
            sendError(ctx, "Game not found");
            return;
        }

        GameData game = gameOpt.get();
        gameConnections.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
        gameConnections.get(gameID).add(ctx);

        try {
            ctx.send(gson.toJson(Map.of(
                    "serverMessageType", "LOAD_GAME",
                    "game", game
            )));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String role;
        if (username.equals(game.whiteUsername())) role = "white";
        else if (username.equals(game.blackUsername())) role = "black";
        else role = "observer";

        broadcastNotification(gameID, username + " connected as " + role, ctx);
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) {
        Integer gameID = cmd.getGameID();
        Optional<AuthData> authOpt;
        try {
            authOpt = authDAO.getAuthByToken(cmd.getAuthToken());
        } catch (DataAccessException e) {
            return;
        }
        if (authOpt.isEmpty()) {
            return;
        }

        String username = authOpt.get().username();
        gameConnections.computeIfPresent(gameID, (id, clients) -> {
            clients.remove(ctx);
            broadcastNotification(gameID, username + " left the game", ctx);
            return clients.isEmpty() ? null : clients;
        });
    }

    private void handleMove(WsContext ctx, UserGameCommand cmd) { }

    private void handleResign(WsContext ctx, UserGameCommand cmd) { }

    private void broadcastNotification(int gameID, String message, WsContext excludeCtx) {
        Set<WsContext> clients = gameConnections.getOrDefault(gameID, Set.of());
        if (clients.isEmpty()) {
            return;
        }

        Map<String, String> payload = Map.of("message", message);
        String json = gson.toJson(payload);
        for (WsContext client : clients) {
            if (client != excludeCtx) {
                try {
                    client.send(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendError(WsContext ctx, String message) {
        try {
            ctx.send(gson.toJson(Map.of("serverMessageType", "ERROR", "message", message)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
