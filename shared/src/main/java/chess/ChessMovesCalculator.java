package chess;

import java.util.Collection;
import java.util.HashSet;

public interface ChessMovesCalculator {

    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos);

    /** Helper for sliding pieces (like rook, bishop, queen). */
    default Collection<ChessMove> calculateMovesHelper(
            ChessBoard board, ChessPosition pos, int[][] directions, Collection<ChessMove> moves) {

        ChessPiece piece = board.getPiece(pos);
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] direction : directions) {
            int dx = row + direction[0];
            int dy = col + direction[1];

            while (ChessBoard.isValidPosition(dx, dy)) {
                ChessPosition newPosition = new ChessPosition(dx, dy);
                boolean result = addValidMoveIfPossible(board, pos, newPosition, piece, moves);

                if (!result) {
                    break;
                }

                ChessPiece newPiece = board.getPiece(newPosition);
                if (newPiece != null && piece.getTeamColor() == newPiece.getTeamColor()) {
                    break;
                }

                dx += direction[0];
                dy += direction[1];
            }
        }
        return moves;
    }

    /** Helper for one-step moves (like King and Knight). */
    default Collection<ChessMove> calculateSingleStepMoves(
            ChessBoard board, ChessPosition pos, int[][] directions, Collection<ChessMove> moves) {

        ChessPiece piece = board.getPiece(pos);
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] direction : directions) {
            int dx = row + direction[0];
            int dy = col + direction[1];

            if (ChessBoard.isValidPosition(dx, dy)) {
                ChessPosition newPosition = new ChessPosition(dx, dy);
                addValidMoveIfPossible(board, pos, newPosition, piece, moves);
            }
        }
        return moves;
    }

    static boolean addValidMoveIfPossible(
            ChessBoard board, ChessPosition currentPos, ChessPosition newPos,
            ChessPiece piece, Collection<ChessMove> moves) {

        ChessPiece newPiece = board.getPiece(newPos);

        if (newPiece != null && piece.getTeamColor() != newPiece.getTeamColor()) {
            moves.add(new ChessMove(currentPos, newPos, null));
            return false;
        }

        if (newPiece == null) {
            moves.add(new ChessMove(currentPos, newPos, null));
        }

        return true;
    }
}

class CalculateKingMoves implements ChessMovesCalculator {

    private static final int[][] DIRECTIONS = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        return calculateSingleStepMoves(board, pos, DIRECTIONS, new HashSet<>());
    }
}

class CalculateQueenMoves implements ChessMovesCalculator {

    private final CalculateBishopMoves bishopCalc = new CalculateBishopMoves();
    private final CalculateRookMoves rookCalc = new CalculateRookMoves();

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();
        moves.addAll(bishopCalc.calculateMoves(board, pos));
        moves.addAll(rookCalc.calculateMoves(board, pos));
        return moves;
    }
}

class CalculateBishopMoves implements ChessMovesCalculator {

    private static final int[][] DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, -1}, {-1, 1}
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        return calculateMovesHelper(board, pos, DIRECTIONS, new HashSet<>());
    }
}

class CalculateKnightMoves implements ChessMovesCalculator {

    private static final int[][] DIRECTIONS = {
            {1, 2}, {2, 1}, {2, -1}, {1, -2},
            {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        return calculateSingleStepMoves(board, pos, DIRECTIONS, new HashSet<>());
    }
}

class CalculateRookMoves implements ChessMovesCalculator {

    private static final int[][] DIRECTIONS = {
            {0, 1}, {1, 0}, {0, -1}, {-1, 0}
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        return calculateMovesHelper(board, pos, DIRECTIONS, new HashSet<>());
    }
}

class CalculatePawnMoves implements ChessMovesCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(pos);
        ChessGame.TeamColor color = piece.getTeamColor();

        int row = pos.getRow();
        int col = pos.getColumn();
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        boolean firstMove = (row == 2 && color == ChessGame.TeamColor.WHITE)
                || (row == 7 && color == ChessGame.TeamColor.BLACK);

        for (int offset = -1; offset <= 1; offset++) {
            int newRow = row + direction;
            int newCol = col + offset;

            if (!ChessBoard.isValidPosition(newRow, newCol)) {
                continue;
            }

            ChessPosition target = new ChessPosition(newRow, newCol);
            ChessPiece targetPiece = board.getPiece(target);

            if (offset == 0) {
                handleForwardMoves(board, pos, target, piece, firstMove, direction, moves);
                continue;
            }

            if (targetPiece != null && piece.getTeamColor() != targetPiece.getTeamColor()) {
                calculatePromotionPiece(moves, pos, target);
            }
        }
        return moves;
    }

    private void handleForwardMoves(
            ChessBoard board, ChessPosition start, ChessPosition oneStep,
            ChessPiece piece, boolean firstMove, int direction, Collection<ChessMove> moves) {

        if (board.getPiece(oneStep) != null) {
            return;
        }

        calculatePromotionPiece(moves, start, oneStep);

        if (!firstMove) {
            return;
        }

        int twoStepRow = oneStep.getRow() + direction;
        ChessPosition twoStep = new ChessPosition(twoStepRow, oneStep.getColumn());

        if (ChessBoard.isValidPosition(twoStepRow, oneStep.getColumn())
                && board.getPiece(twoStep) == null) {
            moves.add(new ChessMove(start, twoStep, null));
        }
    }

    private void calculatePromotionPiece(
            Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {

        if (end.getRow() == 1 || end.getRow() == 8) {
            for (ChessPiece.PieceType type : ChessPiece.PROMOTION_PIECES) {
                moves.add(new ChessMove(start, end, type));
            }
            return;
        }

        moves.add(new ChessMove(start, end, null));
    }
}
