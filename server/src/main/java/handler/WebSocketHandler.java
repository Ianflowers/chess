package handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.websocket.*;
import model.*;
import websocket.commands.*;
import websocket.messages.*;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private final Gson gson;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConcurrentHashMap<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

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
            UserGameCommand baseCmd = gson.fromJson(msg, UserGameCommand.class);
            if (baseCmd == null) {
                sendError(ctx, "Invalid command format");
                return;
            }

            switch (baseCmd.getCommandType()) {
                case CONNECT -> handleConnect(ctx, baseCmd);
                case MAKE_MOVE -> handleMove(ctx, gson.fromJson(msg, MakeMoveCommand.class));
                case LEAVE -> handleLeave(ctx, baseCmd);
                case RESIGN -> handleResign(ctx, baseCmd);
                default -> sendError(ctx, "Unknown command");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ctx, "Invalid command format or server error");
        }
    }

    private void onClose(WsCloseContext ctx) {
        System.out.println("Client disconnected: " + ctx.sessionId());
        gameConnections.forEach((gameID, clients) -> {
            clients.removeIf(ws -> ws.sessionId().equals(ctx.sessionId()));
            broadcastNotification(gameID, "A player disconnected", null);
        });
    }

    private void onError(WsErrorContext ctx) {
        System.out.println("WebSocket error: " + ctx.sessionId());
        ctx.error().printStackTrace();
    }

    // handlers
    private void handleConnect(WsContext ctx, UserGameCommand cmd) {
        Optional<AuthData> authOpt = getAuth(ctx, cmd.getAuthToken());
        if (authOpt.isEmpty()) {
            return;
        }
        String username = authOpt.get().username();

        Optional<GameData> gameOpt = getGame(ctx, cmd.getGameID());
        if (gameOpt.isEmpty()) {
            return;
        }
        GameData game = gameOpt.get();

        gameConnections.computeIfAbsent(cmd.getGameID(), id -> ConcurrentHashMap.newKeySet()).add(ctx);

        try {
            ctx.send(gson.toJson(new LoadGameMessage(game)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String role;
        if (username.equals(game.whiteUsername())) role = "white";
        else if (username.equals(game.blackUsername())) role = "black";
        else role = "observer";

        broadcastNotification(cmd.getGameID(), username + " connected as " + role, ctx);
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) {
        Optional<AuthData> authOpt = getAuth(ctx, cmd.getAuthToken());
        if (authOpt.isEmpty()) {
            return;
        }
        String username = authOpt.get().username();

        gameConnections.computeIfPresent(cmd.getGameID(), (id, clients) -> {
            clients.remove(ctx);
            broadcastNotification(cmd.getGameID(), username + " left the game", ctx);
            return clients.isEmpty() ? null : clients;
        });
    }

    private void handleMove(WsContext ctx, UserGameCommand cmd) throws InvalidMoveException {
        if (!(cmd instanceof MakeMoveCommand moveCmd)) {
            sendError(ctx, "Invalid MAKE_MOVE command");
            return;
        }

        ChessMove move = moveCmd.getMove();

        Optional<AuthData> authOpt = getAuth(ctx, moveCmd.getAuthToken());
        if (authOpt.isEmpty()) {
            return;
        }
        String username = authOpt.get().username();

        Optional<GameData> gameOpt = getGame(ctx, moveCmd.getGameID());
        if (gameOpt.isEmpty()) {
            return;
        }
        GameData game = gameOpt.get();

        if (game.game().getStatus() != ChessGame.Status.ONGOING && game.game().getStatus() != ChessGame.Status.CHECK) {
            sendError(ctx, "Game is already over");
            return;
        }

        Collection<ChessMove> valid = game.game().validMoves(move.getStartPosition());
        if (valid == null || !valid.contains(move)) {
            sendError(ctx, "Illegal move");
            return;
        }

        ChessGame.TeamColor playerColor = username.equals(game.whiteUsername())
                ? ChessGame.TeamColor.WHITE
                : username.equals(game.blackUsername())
                ? ChessGame.TeamColor.BLACK
                : null;

        if (playerColor == null) {
            sendError(ctx, "Observers cannot make moves");
            return;
        }

        if (playerColor != game.game().getTeamTurn()) {
            sendError(ctx, "It's not your turn");
            return;
        }

        game.game().makeMove(move);
        game.game().updateStatusAfterMove();

        try {
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            sendError(ctx, "Server error updating game");
            return;
        }

        Set<WsContext> clients = gameConnections.getOrDefault(moveCmd.getGameID(), Set.of());
        String gameJson = gson.toJson(new LoadGameMessage(game));
        for (WsContext client : clients) {
            try {
                client.send(gameJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String moveMessage = username + " moved from " + move.getStartPosition() +
                " to " + move.getEndPosition();
        broadcastNotification(moveCmd.getGameID(), moveMessage, ctx);

        ChessGame.Status status = game.game().getStatus();
        String statusMessage = switch (status) {
            case CHECK -> "Check!";
            case CHECKMATE -> {
                String winner = (game.game().getTeamTurn() == ChessGame.TeamColor.WHITE)
                        ? game.blackUsername()
                        : game.whiteUsername();
                yield "Checkmate! " + winner + " wins.";
            }
            case STALEMATE -> "Stalemate!";
            default -> null;
        };
        if (statusMessage != null) {
            broadcastNotification(moveCmd.getGameID(), statusMessage, null);
        }
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) {
        Optional<AuthData> authOpt = getAuth(ctx, cmd.getAuthToken());
        if (authOpt.isEmpty()) return;
        String username = authOpt.get().username();

        Optional<GameData> gameOpt = getGame(ctx, cmd.getGameID());
        if (gameOpt.isEmpty()) return;
        GameData game = gameOpt.get();

        game.game().setStatus(ChessGame.Status.RESIGNED);
        try {
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            sendError(ctx, "Server error updating game");
            return;
        }

        String winner;
        if (username.equals(game.whiteUsername())) {
            winner = game.blackUsername();
        } else {
            winner = game.whiteUsername();
        }

        String message = username + " resigned. " + winner + " wins.";

        Set<WsContext> clients = gameConnections.getOrDefault(cmd.getGameID(), Set.of());
        for (WsContext client : clients) {
            try {
                client.send(gson.toJson(new NotificationMessage(message)));
                client.send(gson.toJson(new LoadGameMessage(game)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper Methods
    private Optional<AuthData> getAuth(WsContext ctx, String authToken) {
        try {
            Optional<AuthData> authOpt = authDAO.getAuthByToken(authToken);
            if (authOpt.isEmpty()) {
                sendError(ctx, "Invalid authToken");
            }
            return authOpt;
        } catch (DataAccessException e) {
            sendError(ctx, "Server failure during auth lookup");
            return Optional.empty();
        }
    }

    private Optional<GameData> getGame(WsContext ctx, Integer gameID) {
        try {
            Optional<GameData> gameOpt = gameDAO.getGameById(gameID);
            if (gameOpt.isEmpty()) {
                sendError(ctx, "Game not found");
            }
            return gameOpt;
        } catch (DataAccessException e) {
            sendError(ctx, "Server failure during game lookup");
            return Optional.empty();
        }
    }

    private void broadcastNotification(int gameID, String message, WsContext excludeCtx) {
        Set<WsContext> clients = gameConnections.getOrDefault(gameID, Set.of());
        if (clients.isEmpty()) return;

        String json = gson.toJson(new NotificationMessage(message));

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
            ctx.send(gson.toJson(new ErrorMessage(message)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
