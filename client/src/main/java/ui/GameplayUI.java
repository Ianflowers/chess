package ui;

import chess.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import java.net.http.WebSocket;
import java.util.Collection;

public class GameplayUI {

    private final String reset = EscapeSequences.RESET_TEXT_COLOR;
    private final boolean whitePerspective;
    private WebSocket webSocket;
    private ChessGame game;
    private ChessMove move;

    public GameplayUI(boolean whitePerspective, WebSocket webSocket) {
        this.whitePerspective = whitePerspective;
        this.webSocket = webSocket;
    }

    public void setWebSocket(WebSocket ws) { this.webSocket = ws;}

    public void redrawBoard(ChessGame game) {
        this.game = game;
        BoardDrawer.drawBoard(whitePerspective, game, null, null);
    }

    public void showNotification(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "[NOTICE] " + message + reset + "\n");
    }

    public void showError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "[ERROR] " + message + reset + "\n");
    }

    public void onDisconnect() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "[DISCONNECTED] Lost connection to server."
                + reset + "\n");
    }

    public Result handlePlayer(String[] parts, String authToken, int gameID) {
        Result result = Result.none();
        switch (parts[0].toLowerCase()) {
            case "help" -> printHelpPlayer();
            case "redraw" -> redrawBoard(game);
            case "highlight" -> handleHighlight(parts);
            case "move" -> result = handleMove(parts);
            case "leave" -> result = sendLeave(authToken, gameID);
            case "resign" -> result = Result.resign();
            default -> showError("\nUnknown command. Type 'help' for a list of commands.\n");
        }
        return result;
    }

    public Result handleObserver(String[] parts, String authToken, int gameID) {
        Result result = Result.none();
        switch (parts[0].toLowerCase()) {
            case "help" -> printHelpObserver();
            case "redraw" -> redrawBoard(game);
            case "highlight" -> handleHighlight(parts);
            case "leave" -> result = sendLeave(authToken, gameID);
            default -> showError("\nUnknown command. Type 'help' for a list of commands.\n");
        }
        return result;
    }

    private void printHelpPlayer() {
        System.out.println("""
                help                    - Show this help text
                redraw                  - Redraw the chess board
                highlight <square>      - Highlight legal moves for a piece, e.g., highlight b1
                move <from> <to>        - Make a move, e.g., move e2 e4
                leave                   - Leave the game
                resign                  - Resign the game
                """);
    }

    private void printHelpObserver() {
        System.out.println("""
                help                    - Show this help text
                redraw                  - Redraw the chess board
                highlight <square>      - Highlight legal moves for a piece, e.g., highlight b1
                leave                   - Leave the game
                """);
    }

    private Result sendLeave(String authToken, int gameID) {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
        return Result.quit();
    }

    public void sendResign(String authToken, int gameID) {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private Result handleMove(String[] parts) {
        if (parts.length != 3) {
            showError("Invalid move command. Format: move <from> <to>\n");
            return Result.none();
        }

        char[] startPos = parts[1].toCharArray();
        char[] endPos = parts[2].toCharArray();

        if (!validInput(startPos) || !validInput(endPos)) {
            showError("Invalid square(s). Must be like e2 or h7.\n");
            return Result.none();
        }

        ChessPosition start = new ChessPosition(Character.getNumericValue(startPos[1]), fileToColumn(startPos[0]));
        ChessPosition end = new ChessPosition(Character.getNumericValue(endPos[1]), fileToColumn(endPos[0]));

        ChessPiece piece = game.getBoard().getPiece(start);
        if (piece == null) {
            showError("No piece at that starting square.\n");
            return Result.none();
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && (end.getRow() == 1 || end.getRow() == 8)) {
            return Result.promote();
        }

        move = new ChessMove(start, end, null);
        return Result.move();
    }

    public boolean handlePromotion(String choice) {
        ChessPiece.PieceType promo;
        switch (choice) {
            case "q", "queen" -> promo = ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> promo = ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> promo = ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> promo = ChessPiece.PieceType.KNIGHT;
            default -> {
                System.out.println("Invalid choice.\n");
                return true;
            }
        }

        move = new ChessMove(move.getStartPosition(), move.getEndPosition(), promo);
        return false;
    }

    public void finishMove(String authToken, int gameID) {
        sendCommand(new MakeMoveCommand(authToken, gameID, move));
        move = null;
    }

    private void handleHighlight(String[] parts) {
        if (game == null) {
            showError("No game loaded.\n");
            return;
        }

        if (parts.length != 2) {
            showError("Usage: highlight <square>\n");
            return;
        }

        char[] square = parts[1].toCharArray();
        if (!validInput(square)) {
            showError("Invalid square. Must be like e2.\n");
            return;
        }


        ChessPosition selected = new ChessPosition(Character.getNumericValue(square[1]), fileToColumn(square[0]));

        if (game.getBoard().getPiece(selected) == null) {
            return;
        }

        Collection<ChessMove> moves = game.validMoves(selected);
        Collection<ChessPosition> highlightSquares = moves.stream().map(ChessMove::getEndPosition).toList();

        BoardDrawer.drawBoard(whitePerspective, game, highlightSquares, selected);
    }

    private void sendCommand(UserGameCommand cmd) {
        try {
            String json = new com.google.gson.Gson().toJson(cmd);
            webSocket.sendText(json, true);
        } catch (Exception e) {
            showError("Failed to send command: " + e.getMessage());
        }
    }

    private boolean validInput(char[] input) {
        if (input.length != 2) {
            return false;
        }
        return ChessBoard.isValidPosition(Character.getNumericValue(input[1]), fileToColumn(input[0]));
    }

    private int fileToColumn(char file) {
        file = Character.toLowerCase(file);
        if (file < 'a' || file > 'h') {
            return 0;
        }
        return (file - 'a') + 1;
    }

}
