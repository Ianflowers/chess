//package ui;
//
//public class BoardDrawer {
//    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
//    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREY;
//    private static final String RESET_COLORS = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;
//
//    private static final String WHITE_VIEW = "    a  b  c  d  e  f  g  h";
//    private static final String BLACK_VIEW = "    h  g  f  e  d  c  b  a";
//
//    public static void drawBoard(boolean whitePerspective) {
//        String lettering = whitePerspective ? WHITE_VIEW : BLACK_VIEW;
//        int startRow = whitePerspective ? 8 : 1;
//        int endRow = whitePerspective ? 1 : 8;
//        int rowStep = whitePerspective ? -1 : 1;
//
//        System.out.print(EscapeSequences.ERASE_SCREEN);
//        System.out.print("\n" + lettering + "\n");
//
//        for (int row = startRow; whitePerspective ? row >= endRow : row <= endRow; row += rowStep) {
//            System.out.print(" " + row + " ");
//            for (int i = 0; i < 8; i++) {
//                int col = whitePerspective ? i + 1 : 8 - i;
//                boolean isDark = (row + col) % 2 == 0;
//                String bgColor = isDark ? DARK_SQUARE : LIGHT_SQUARE;
//                String piece = getStartingPiece(row, col);
//                System.out.print(bgColor + EscapeSequences.SET_TEXT_COLOR_WHITE + piece + RESET_COLORS);
//            }
//            System.out.print(" " + row + "\n");
//        }
//
//        System.out.println(lettering);
//        System.out.print(RESET_COLORS);
//    }
//
//    private static String getStartingPiece(int row, int col) {
//        if (row == 8) {
//            return switch (col) {
//                case 1, 8 -> EscapeSequences.BLACK_ROOK;
//                case 2, 7 -> EscapeSequences.BLACK_KNIGHT;
//                case 3, 6 -> EscapeSequences.BLACK_BISHOP;
//                case 4 -> EscapeSequences.BLACK_QUEEN;
//                case 5 -> EscapeSequences.BLACK_KING;
//                default -> EscapeSequences.EMPTY;
//            };
//        }
//
//        if (row == 7) {
//            return EscapeSequences.BLACK_PAWN;
//        }
//
//        if (row == 1) {
//            return switch (col) {
//                case 1, 8 -> EscapeSequences.WHITE_ROOK;
//                case 2, 7 -> EscapeSequences.WHITE_KNIGHT;
//                case 3, 6 -> EscapeSequences.WHITE_BISHOP;
//                case 4 -> EscapeSequences.WHITE_QUEEN;
//                case 5 -> EscapeSequences.WHITE_KING;
//                default -> EscapeSequences.EMPTY;
//            };
//        }
//        if (row == 2) {
//            return EscapeSequences.WHITE_PAWN;
//        }
//
//        return EscapeSequences.EMPTY;
//    }
//}


package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardDrawer {
    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREY;
    private static final String RESET_COLORS = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;

    private static final String WHITE_VIEW = "    a  b  c  d  e  f  g  h";
    private static final String BLACK_VIEW = "    h  g  f  e  d  c  b  a";

    public static void drawBoard(boolean whitePerspective, ChessGame game) {
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
                String bgColor = isDark ? DARK_SQUARE : LIGHT_SQUARE;
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
