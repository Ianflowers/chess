package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private static final calculateKingMoves kingCalc = new calculateKingMoves();
    private static final calculateBishopMoves bishopCalc = new calculateBishopMoves();
    private static final calculateKnightMoves knightCalc = new calculateKnightMoves();
    private static final calculateRookMoves rookCalc = new calculateRookMoves();

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE;
        board.resetBoard();;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() { return teamTurn; }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) { teamTurn = team; }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor { WHITE, BLACK }


    private boolean isSquareUnderAttack(ChessPosition position, TeamColor team) {
        TeamColor opponent = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        Collection<ChessMove> pawnThreats = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        int r = (team == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int targetRow = row + r;

        if (ChessBoard.isValidPosition(row + r, col - 1)) {
            pawnThreats.add(new ChessMove(position, new ChessPosition(row + r, col - 1), null));
        }
        if (ChessBoard.isValidPosition(row + r, col + 1)) {
            pawnThreats.add(new ChessMove(position, new ChessPosition(row + r, col + 1), null));
        }

        if (containsThreateningPiece(bishopCalc.calculateMoves(board, position), opponent, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN)) return true;
        if (containsThreateningPiece(knightCalc.calculateMoves(board, position), opponent, ChessPiece.PieceType.KNIGHT)) return true;
        if (containsThreateningPiece(rookCalc.calculateMoves(board, position), opponent, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.QUEEN)) return true;
        if (containsThreateningPiece(kingCalc.calculateMoves(board, position), opponent, ChessPiece.PieceType.KING)) return true;
        if (containsThreateningPiece(pawnThreats, opponent, ChessPiece.PieceType.PAWN)) return true;

        return false;
    }

    private boolean containsThreateningPiece(Collection<ChessMove> moves, TeamColor opponent, ChessPiece.PieceType... threatTypes) {
        for (ChessMove move : moves) {
            ChessPiece piece = board.getPiece(move.getEndPosition());
            if (piece != null && piece.getTeamColor() == opponent) {
                for (ChessPiece.PieceType threatType : threatTypes) {
                    if (piece.getPieceType() == threatType) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;

        Collection<ChessMove> allMoves = piece.pieceMoves(this.board, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : allMoves) {
            ChessGame gameCopy = copy();

            try {
                gameCopy.makeMove(move);
                ChessPosition kingPosition = gameCopy.board.getKingPosition(piece.getTeamColor());

                if (!gameCopy.isSquareUnderAttack(kingPosition, piece.getTeamColor())) {
                    validMoves.add(move);
                }
            } catch (InvalidMoveException e) { }

        }

        return validMoves;
    }

    public boolean hasAnyValidMove(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(position).isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

        /**
         * Makes a move in a chess game
         *
         * @param move chess move to perform
         * @throws InvalidMoveException if move is invalid
         */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) throw new InvalidMoveException("No piece at start position.");
        if (piece.getTeamColor() != teamTurn) throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");

        board.addPiece(end, piece);
        board.addPiece(start, null);

        // 5. Handle promotion (if it's a pawn and move has a promotion type)
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getPromotionPiece() != null) {
            board.addPiece(end, new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) { return isSquareUnderAttack(this.board.getKingPosition(teamColor), teamColor); }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) { return isInCheck(teamColor) && !hasAnyValidMove(teamColor); }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) { return !isInCheck(teamColor) && !hasAnyValidMove(teamColor); }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) { this.board = board; }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() { return board;}

    public ChessGame copy() {
        ChessGame newGame = new ChessGame();
        newGame.teamTurn = this.teamTurn;
        newGame.board = this.board.copy();
        return newGame;
    }


}
