package ui;

import chess.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.Scanner;

public class GameplayUI {

    private final boolean whitePerspective;
    private final WebSocket webSocket;
    private final Scanner scanner = new Scanner(System.in);
    private ChessGame game;
    private boolean isActive;

    public GameplayUI(boolean whitePerspective, WebSocket webSocket) {
        this.whitePerspective = whitePerspective;
        this.webSocket = webSocket;
        this.isActive = true;
    }

    public void redrawBoard(ChessGame game) {
        this.game = game;
        BoardDrawer.drawBoard(whitePerspective, game, null, null);
    }

    public void showNotification(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "[NOTICE] " + message + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void showError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "[ERROR] " + message + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void onDisconnect() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "[DISCONNECTED] Lost connection to server." + EscapeSequences.RESET_TEXT_COLOR);
        isActive = false;
    }

    public void run(String authToken, int gameID) {

        while (isActive) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            try {
                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                } else if (input.equalsIgnoreCase("redraw")) {
                    if (game != null) {
                        redrawBoard(game);
                    }
                } else if (input.equalsIgnoreCase("leave")) {
                    sendLeaveCommand(authToken, gameID);
                    isActive = false;
                } else if (input.equalsIgnoreCase("resign")) {
                    sendResignCommand(authToken, gameID);
                    isActive = false;
                } else if (input.toLowerCase().startsWith("move")) {
                    handleMoveCommand(input, authToken, gameID);
                } else if (input.toLowerCase().startsWith("highlight")) {
                    handleHighlightCommand(input);
                } else {
                    showError("Unknown command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                showError("Error processing command: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                help                    - Show this help text
                redraw                  - Redraw the chess board
                leave                   - Leave the game
                resign                  - Resign the game
                move <from> <to>        - Make a move, e.g., move e2 e4
                highlight <square>      - Highlight legal moves for a piece, e.g., highlight b1
                """);
    }

    private void sendLeaveCommand(String authToken, int gameID) {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    private void sendResignCommand(String authToken, int gameID) {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void handleMoveCommand(String input, String authToken, int gameID) {

        String[] parts = input.split("\\s+");
        if (parts.length != 3) {
            showError("Invalid move command. Format: move <from> <to>");
            return;
        }

        char[] startPos = parts[1].toCharArray();
        char[] endPos = parts[2].toCharArray();

        if (!validInput(startPos) || !validInput(endPos)) {
            showError("Invalid square(s). Must be like e2 or h7.");
            return;
        }

        ChessPosition start = new ChessPosition(Character.getNumericValue(startPos[1]), fileToColumn(startPos[0]));
        ChessPosition end = new ChessPosition(Character.getNumericValue(endPos[1]), fileToColumn(endPos[0]));

        ChessPiece piece = game.getBoard().getPiece(start);
        if (piece == null) {
            showError("No piece at that starting square.");
            return;
        }

        ChessPiece.PieceType promo = null;

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && (end.getRow() == 1 || end.getRow() == 8)) {
            while (true) {
                System.out.print("Promote pawn to (Q, R, B, N): ");
                String choice = scanner.nextLine().trim().toUpperCase();

                switch (choice) {
                    case "Q", "QUEEN" -> promo = ChessPiece.PieceType.QUEEN;
                    case "R", "ROOK" -> promo = ChessPiece.PieceType.ROOK;
                    case "B", "BISHOP" -> promo = ChessPiece.PieceType.BISHOP;
                    case "N", "KNIGHT" -> promo = ChessPiece.PieceType.KNIGHT;
                    default -> {
                        System.out.println("Invalid choice.");
                        continue;
                    }
                }
                break;
            }
        }

        ChessMove move = new ChessMove(start, end, promo);
        sendCommand(new MakeMoveCommand(authToken, gameID, move));
    }

    private void handleHighlightCommand(String input) {
        if (game == null) {
            showError("No game loaded.");
            return;
        }

        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            showError("Usage: highlight <square>");
            return;
        }

        char[] square = parts[1].toCharArray();
        if (!validInput(square)) {
            showError("Invalid square. Must be like e2.");
            return;
        }

        ChessPosition selected = new ChessPosition(Character.getNumericValue(square[1]), fileToColumn(square[0]));
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
