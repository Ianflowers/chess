package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    final private ChessPiece[][] board = new ChessPiece[8][8];


    public ChessBoard() { }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) { return board[position.getRow() - 1][position.getColumn() - 1]; }

    static public boolean isValidPosition(int row, int col) { return row >= 1 && row <= 8 && col >= 1 && col <= 8; }

    public ChessPosition getKingPosition(ChessGame.TeamColor team) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = getPiece(position);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    if (piece.getTeamColor() == team) {
                        return position;
                    }
                }
            }
        }
        return null;
    }

    public ChessBoard copy() {
        ChessBoard newBoard = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                newBoard.board[row - 1][col - 1] = board[row - 1][col - 1];
            }
        }

        return newBoard;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }

        ChessPiece.PieceType[] pieceOrder = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        for (int c = 1; c < 9; c++) {
            addPiece(new ChessPosition(1, c), new ChessPiece(ChessGame.TeamColor.WHITE, pieceOrder[c - 1]));
            addPiece(new ChessPosition(2, c), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, c), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(8, c), new ChessPiece(ChessGame.TeamColor.BLACK, pieceOrder[c - 1]));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() { return Arrays.deepHashCode(board); }

}
