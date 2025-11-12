package ui;

public class BoardDrawer {
    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREY;
    private static final String RESET_COLORS = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;

    public static void drawWhitePerspective() {
        System.out.print(EscapeSequences.ERASE_SCREEN);

        System.out.print("\n    a  b  c  d  e  f  g  h\n");

        for (int row = 8; row >= 1; row--) {
            System.out.print(" " + row + " ");

            for (int col = 1; col <= 8; col++) {
                boolean isLight = (row + col) % 2 == 0;
                String bgColor = isLight ? LIGHT_SQUARE : DARK_SQUARE;
                String piece = getStartingPiece(row, col);

                System.out.print(bgColor + EscapeSequences.SET_TEXT_COLOR_BLACK + piece + RESET_COLORS);
            }

            System.out.print(" " + row + "\n");
        }

        System.out.println("    a  b  c  d  e  f  g  h");
        System.out.print(RESET_COLORS);
    }

    private static String getStartingPiece(int row, int col) {
        if (row == 8) return switch (col) {
            case 1, 8 -> EscapeSequences.BLACK_ROOK;
            case 2, 7 -> EscapeSequences.BLACK_KNIGHT;
            case 3, 6 -> EscapeSequences.BLACK_BISHOP;
            case 4 -> EscapeSequences.BLACK_QUEEN;
            case 5 -> EscapeSequences.BLACK_KING;
            default -> EscapeSequences.EMPTY;
        };

        if (row == 7) return EscapeSequences.BLACK_PAWN;

        if (row == 1) return switch (col) {
            case 1, 8 -> EscapeSequences.WHITE_ROOK;
            case 2, 7 -> EscapeSequences.WHITE_KNIGHT;
            case 3, 6 -> EscapeSequences.WHITE_BISHOP;
            case 4 -> EscapeSequences.WHITE_QUEEN;
            case 5 -> EscapeSequences.WHITE_KING;
            default -> EscapeSequences.EMPTY;
        };
        if (row == 2) return EscapeSequences.WHITE_PAWN;

        return EscapeSequences.EMPTY;
    }
}
