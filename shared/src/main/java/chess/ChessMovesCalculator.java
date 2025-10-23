package chess;

import java.util.Collection;
import java.util.HashSet;

public interface ChessMovesCalculator {
    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos);

    default Collection<ChessMove> calculateMovesHelper(
            ChessBoard board, ChessPosition pos, int[][] directions, Collection<ChessMove> moves) {

        ChessPiece piece = board.getPiece(pos);
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] dir : directions) {
            addMovesInDirection(board, pos, piece, row, col, dir, moves);
        }
        return moves;
    }

    private void addMovesInDirection(
            ChessBoard board, ChessPosition pos, ChessPiece piece,
            int row, int col, int[] dir, Collection<ChessMove> moves) {

        int dx = row + dir[0];
        int dy = col + dir[1];

        while (ChessBoard.isValidPosition(dx, dy)) {
            ChessPosition newPos = new ChessPosition(dx, dy);
            boolean canContinue = addValidMoveIfPossible(board, pos, newPos, piece, moves);
            if (!canContinue) {
                break;
            }

            ChessPiece newPiece = board.getPiece(newPos);
            if (newPiece != null && piece.getTeamColor() == newPiece.getTeamColor()) {
                break;
            }

            dx += dir[0];
            dy += dir[1];
        }
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
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] dirs = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        ChessPiece piece = board.getPiece(pos);
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] d : dirs) {
            int dx = row + d[0];
            int dy = col + d[1];
            if (!ChessBoard.isValidPosition(dx, dy)) continue;
            ChessPosition newPos = new ChessPosition(dx, dy);
            ChessMovesCalculator.addValidMoveIfPossible(board, pos, newPos, piece, moves);
        }
        return moves;
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
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        int[][] dirs = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        return calculateMovesHelper(board, pos, dirs, new HashSet<>());
    }
}

class CalculateKnightMoves implements ChessMovesCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(pos);
        int[][] dirs = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
        int row = pos.getRow();
        int col = pos.getColumn();

        for (int[] d : dirs) {
            int dx = row + d[0];
            int dy = col + d[1];
            if (!ChessBoard.isValidPosition(dx, dy)) continue;
            ChessPosition newPos = new ChessPosition(dx, dy);
            ChessMovesCalculator.addValidMoveIfPossible(board, pos, newPos, piece, moves);
        }
        return moves;
    }
}

class CalculateRookMoves implements ChessMovesCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        return calculateMovesHelper(board, pos, dirs, new HashSet<>());
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

        for (int c = -1; c <= 1; c++) {
            int newRow = row + direction;
            int newCol = col + c;
            if (!ChessBoard.isValidPosition(newRow, newCol)) continue;

            ChessPosition nextPos = new ChessPosition(newRow, newCol);
            ChessPiece occupant = board.getPiece(nextPos);

            if (c == 0) {
                handleForwardMoves(board, pos, nextPos, firstMove, direction, occupant, moves);
            } else {
                handleCaptureMoves(piece, pos, nextPos, occupant, moves);
            }
        }
        return moves;
    }

    private void handleForwardMoves(
            ChessBoard board, ChessPosition start, ChessPosition oneStep,
            boolean firstMove, int dir, ChessPiece occupant, Collection<ChessMove> moves) {

        if (occupant != null) return;
        calculatePromotionPiece(moves, start, oneStep);

        if (!firstMove) return;

        int twoRow = oneStep.getRow() + dir;
        int col = oneStep.getColumn();
        if (!ChessBoard.isValidPosition(twoRow, col)) return;

        ChessPosition twoStep = new ChessPosition(twoRow, col);
        if (board.getPiece(twoStep) == null) {
            moves.add(new ChessMove(start, twoStep, null));
        }
    }

    private void handleCaptureMoves(
            ChessPiece piece, ChessPosition start, ChessPosition target,
            ChessPiece occupant, Collection<ChessMove> moves) {

        if (occupant != null && piece.getTeamColor() != occupant.getTeamColor()) {
            calculatePromotionPiece(moves, start, target);
        }
    }

    void calculatePromotionPiece(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        int row = end.getRow();
        if (row == 1 || row == 8) {
            for (ChessPiece.PieceType type : ChessPiece.PROMOTION_PIECES) {
                moves.add(new ChessMove(start, end, type));
            }
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
