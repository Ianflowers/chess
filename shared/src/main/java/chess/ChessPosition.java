package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row, col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() { return row; }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() { return col; }

    /**
     * @return whether position coordinates
     * are valid
     */
    static public boolean isValidPosition(int row, int col) { return row >= 1 && row <= 8 && col >= 1 && col <= 8; }

    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ChessPosition that = (ChessPosition) o;

        if (row != that.row) { return false; }
        return col == that.col;
    }

    @Override
    public int hashCode() { return 31 * row + col; }

}
