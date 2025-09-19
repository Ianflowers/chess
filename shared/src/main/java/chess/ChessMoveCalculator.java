package chess;

import java.util.Collection;
import java.util.HashSet;

public interface ChessMoveCalculator {

    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position);

}


class KingMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor kingColor = board.getPiece(position).getTeamColor();

        for (int r = -1; r <= 1; r++) {
            for (int c = -1; c <= 1; c++) {
                int newRow = position.getRow() + r;
                int newCol = position.getColumn() + c;

                if (ChessPosition.isValidPosition(newRow, newCol)) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece newPositionPiece = board.getPiece(newPosition);

                    if (newPositionPiece == null || newPositionPiece.getTeamColor() != kingColor) {
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                }
            }
        }

        return moves;
    }
}

class QueenMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return null;
    }
}

class BishopMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor bishopColor = board.getPiece(position).getTeamColor();
        int row = position.getRow();
        int col = position.getColumn();

        int[][] directions = {
                {1, 1},     // up-left
                {-1, 1},    // up-right
                {-1, -1},   // down-left
                {1, -1}     // down-right
        };

        for (int[] direction : directions) {
            int dr = direction[0];
            int dc = direction[1];

            int newRow = row + dr;
            int newCol = col + dc;

            while(ChessPosition.isValidPosition(newRow, newCol)) {

                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece newPositionPiece = board.getPiece(newPosition);

                if (newPositionPiece == null) {
                    moves.add(new ChessMove(position, newPosition, null));

                } else {
                    if (newPositionPiece.getTeamColor() != bishopColor) {
                        moves.add(new ChessMove(position, newPosition, null));
                    }
                    break;
                }
                newRow += dr;
                newCol += dc;
            }
        }

        return moves;
    }
}

class KnightMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return null;
    }
}

class RookMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return null;
    }

}

class PawnMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        return null;
    }
}