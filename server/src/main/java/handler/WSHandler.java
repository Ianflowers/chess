package handler;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsConfig;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.concurrent.ConcurrentHashMap;

public class WSHandler {

    private final Gson gson;
    private final ConcurrentHashMap<String, WsContext> activeSessions = new ConcurrentHashMap<>();
    public final java.util.function.Consumer<WsConfig> ws;

    public WSHandler(Gson gson) {
        this.gson = gson;

        ws = ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("WebSocket connected: " + ctx);
            });

            ws.onMessage(ctx -> handleMessage(ctx, ctx.message()));

            ws.onClose(ctx -> {
                activeSessions.values().remove(ctx);
                System.out.println("WebSocket closed: " + ctx);
            });

            ws.onError(ctx -> System.err.println("WebSocket error: " + ctx));
        };
    }

    private void handleMessage(WsContext ctx, String message) {
        try {
            UserGameCommand cmd = gson.fromJson(message, UserGameCommand.class);

            switch (cmd.getCommandType()) {
                case CONNECT -> handleConnect(ctx, cmd);
                case MAKE_MOVE -> handleMove(cmd);
                case LEAVE -> handleLeave(cmd);
                case RESIGN -> handleResign(cmd);
                default -> ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR)));
            } catch (Exception ignored) {}
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand cmd) {
        String authToken = cmd.getAuthToken();
        if (authToken != null && !authToken.isEmpty()) {
            activeSessions.put(authToken, ctx);
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME)));
            } catch (Exception ignored) {}
        } else {
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR)));
            } catch (Exception ignored) {}
        }
    }

    private void handleMove(UserGameCommand cmd) {
        Integer gameId = cmd.getGameID();
        if (gameId == null) {
            return;
        }

        // TODO: move processing here?
        broadcastToGame(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION));
    }

    private void handleLeave(UserGameCommand cmd) {
        if (cmd.getAuthToken() != null) {
            activeSessions.remove(cmd.getAuthToken());
        }
    }

    private void handleResign(UserGameCommand cmd) {
        Integer gameId = cmd.getGameID();
        if (gameId != null) {
            broadcastToGame(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION));
        }
    }

    private void broadcastToGame(ServerMessage msg) {
        // TODO: authTokens from GameService--query GameService for player authTokens?
        activeSessions.values().forEach(ctx -> {
            try {
                ctx.send(gson.toJson(msg));
            } catch (Exception ignored) {}
        });
    }
}
