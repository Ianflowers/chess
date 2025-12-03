package handler;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private final Gson gson;
    private final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    public WebSocketHandler(Gson gson) {
        this.gson = gson;
    }

    public void configureWs(io.javalin.websocket.WsConfig ws) {
        ws.onConnect(this::onConnect);
        ws.onMessage(this::onMessage);
        ws.onClose(this::onClose);
        ws.onError(this::onError);
    }

    private void onConnect(WsConnectContext ctx) {
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
            try {
                ctx.send(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.ERROR)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void onClose(WsCloseContext ctx) {
        System.out.println("Client disconnected: " + ctx.sessionId());
        gameConnections.values().forEach(set -> set.remove(ctx));
    }

    private void onError(WsErrorContext ctx) {
        System.out.println("WebSocket error: " + ctx.sessionId());
    }

    // Command handlers
    private void handleConnect(WsContext ctx, UserGameCommand cmd) {}
    private void handleMove(WsContext ctx, UserGameCommand cmd) {}
    private void handleLeave(WsContext ctx, UserGameCommand cmd) {}
    private void handleResign(WsContext ctx, UserGameCommand cmd) {}

}
