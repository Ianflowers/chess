package ui;

import chess.ChessGame;

public class GameplayUI {

    private final boolean whitePerspective;

    public GameplayUI(boolean whitePerspective) {
        this.whitePerspective = whitePerspective;
    }

    public void redrawBoard(ChessGame game) {
        BoardDrawer.drawBoard(whitePerspective, game);
    }

    public void showNotification(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "[NOTICE] " + message + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void showError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "[ERROR] " + message + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void onDisconnect() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "[DISCONNECTED] Lost connection to server." + EscapeSequences.RESET_TEXT_COLOR);
    }

}
