package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import java.util.Collection;

public class BoardDrawer {
    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREY;
    private static final String HIGHLIGHT_SELECTED_SQUARE = EscapeSequences.SET_BG_COLOR_YELLOW;

    private static final String HIGHLIGHT_LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_GREEN;
    private static final String HIGHLIGHT_DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
    private static final String RESET_COLORS = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;


    private static final String WHITE_VIEW = "    a  b  c  d  e  f  g  h";
    private static final String BLACK_VIEW = "    h  g  f  e  d  c  b  a";

    public static void drawBoard(boolean whitePerspective, ChessGame game,
                                 Collection<ChessPosition> highlights, ChessPosition selected) {
        String lettering = whitePerspective ? WHITE_VIEW : BLACK_VIEW;
        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 1 : 8;
        int rowStep = whitePerspective ? -1 : 1;

        System.out.print(EscapeSequences.ERASE_SCREEN);
        System.out.print("\n" + lettering + "\n");

        for (int row = startRow; whitePerspective ? row >= endRow : row <= endRow; row += rowStep) {
            System.out.print(" " + row + " ");
            for (int i = 0; i < 8; i++) {
                int col = whitePerspective ? i + 1 : 8 - i;
                boolean isDark = (row + col) % 2 == 0;

                ChessPosition currentPos = new ChessPosition(row, col);
                String bgColor;

                if (highlights != null && currentPos.equals(selected)) {
                    bgColor = HIGHLIGHT_SELECTED_SQUARE;
                } else if (highlights != null && highlights.contains(currentPos)) {
                    bgColor = isDark ? HIGHLIGHT_DARK_SQUARE : HIGHLIGHT_LIGHT_SQUARE;
                } else {
                    bgColor = isDark ? DARK_SQUARE : LIGHT_SQUARE;
                }

                String pieceSymbol = getPieceSymbol(game, row, col);
                System.out.print(bgColor + EscapeSequences.SET_TEXT_COLOR_WHITE + pieceSymbol + RESET_COLORS);
            }
            System.out.print(" " + row + "\n");
        }

        System.out.println(lettering);
        System.out.print(RESET_COLORS);
    }

    private static String getPieceSymbol(ChessGame game, int row, int col) {
        ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }

}
