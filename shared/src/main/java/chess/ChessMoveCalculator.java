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
        return null;
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