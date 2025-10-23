package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessGame {

    private static final CalculateKingMoves KING_CALC = new CalculateKingMoves();
    private static final CalculateBishopMoves BISHOP_CALC = new CalculateBishopMoves();
    private static final CalculateKnightMoves KNIGHT_MOVES = new CalculateKnightMoves();
    private static final CalculateRookMoves ROOK_CALC = new CalculateRookMoves();

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE;
        board.resetBoard();
    }

    /** @return Which team's turn it is */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which team's turn it is.
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /** Enum identifying the 2 possible teams in a chess game. */
    public enum TeamColor { WHITE, BLACK }

    private boolean isSquareUnderAttack(ChessPosition position, TeamColor team) {
        TeamColor opponent = (team == TeamColor.WHITE)
                ? TeamColor.BLACK
                : TeamColor.WHITE;

        int row = position.getRow();
        int col = position.getColumn();
        int r = (team == TeamColor.WHITE) ? 1 : -1;

        Collection<ChessMove> pawnThreats = new HashSet<>();

        if (ChessBoard.isValidPosition(row + r, col - 1)) {
            pawnThreats.add(new ChessMove(position, new ChessPosition(row + r, col - 1), null));
        }
        if (ChessBoard.isValidPosition(row + r, col + 1)) {
            pawnThreats.add(new ChessMove(position, new ChessPosition(row + r, col + 1), null));
        }

        if (containsThreateningPiece(
                BISHOP_CALC.calculateMoves(board, position),
                opponent,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN)) {
            return true;
        }
        if (containsThreateningPiece(
                KNIGHT_MOVES.calculateMoves(board, position),
                opponent,
                ChessPiece.PieceType.KNIGHT)) {
            return true;
        }
        if (containsThreateningPiece(
                ROOK_CALC.calculateMoves(board, position),
                opponent,
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.QUEEN)) {
            return true;
        }
        if (containsThreateningPiece(
                KING_CALC.calculateMoves(board, position),
                opponent,
                ChessPiece.PieceType.KING)) {
            return true;
        }
        return containsThreateningPiece(pawnThreats, opponent, ChessPiece.PieceType.PAWN);
    }

    private boolean containsThreateningPiece(
            Collection<ChessMove> moves,
            TeamColor opponent,
            ChessPiece.PieceType... threatTypes) {

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
     * Gets valid moves for a piece at the given location.
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> allMoves = piece.pieceMoves(this.board, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : allMoves) {
            ChessGame gameCopy = copy();

            gameCopy.board.addPiece(move.getEndPosition(), piece);
            gameCopy.board.addPiece(startPosition, null);

            if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                    && move.getPromotionPiece() != null) {
                gameCopy.board.addPiece(
                        move.getEndPosition(),
                        new ChessPiece(piece.getTeamColor(), move.getPromotionPiece())
                );
            }

            ChessPosition kingPos = gameCopy.board.getKingPosition(piece.getTeamColor());
            if (!gameCopy.isSquareUnderAttack(kingPos, piece.getTeamColor())) {
                validMoves.add(move);
            }
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
     * Makes a move in a chess game.
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        Collection<ChessMove> moves = validMoves(start);
        ChessPiece piece = board.getPiece(start);

        if (moves == null) {
            throw new InvalidMoveException("No piece at start position.");
        }
        if (!moves.contains(move)) {
            throw new InvalidMoveException("Illegal move for this piece.");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");
        }

        board.addPiece(end, piece);
        board.addPiece(start, null);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                && move.getPromotionPiece() != null) {
            board.addPiece(
                    end,
                    new ChessPiece(piece.getTeamColor(), move.getPromotionPiece())
            );
        }

        teamTurn = (teamTurn == TeamColor.WHITE)
                ? TeamColor.BLACK
                : TeamColor.WHITE;
    }

    /** Determines if the given team is in check. */
    public boolean isInCheck(TeamColor teamColor) {
        return isSquareUnderAttack(board.getKingPosition(teamColor), teamColor);
    }

    /** Determines if the given team is in checkmate. */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasAnyValidMove(teamColor);
    }

    /** Determines if the given team is in stalemate. */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasAnyValidMove(teamColor);
    }

    /** Sets this game's chessboard. */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /** Gets the current chessboard. */
    public ChessBoard getBoard() {
        return board;
    }

    public ChessGame copy() {
        ChessGame newGame = new ChessGame();
        newGame.teamTurn = this.teamTurn;
        newGame.board = this.board.copy();
        return newGame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        int result = (teamTurn != null) ? teamTurn.hashCode() : 0;
        result = 31 * result + ((board != null) ? board.hashCode() : 0);
        return result;
    }
}
