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
        Collection<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor queenColor = board.getPiece(position).getTeamColor();
        int row = position.getRow();
        int col = position.getColumn();

        int[][] directions = {
                {0, 1},     // up
                {-1, 1},    // up-right
                {1, 0},     // right
                {1, -1},    // down-right
                {0, -1},    // down
                {-1, -1},   // down-left
                {-1, 0},    // left
                {1, 1},     // up-left
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
                    if (newPositionPiece.getTeamColor() != queenColor) {
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
        Collection<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor knightColor = board.getPiece(position).getTeamColor();
        int row = position.getRow();
        int col = position.getColumn();

        int[][] directions = {  // 2  1
                {1, 2},         // up right
                {-1, 2},        // up left
                {2, 1},         // right up
                {2, -1},        // right down
                {1, -2},        // down right
                {-1, -2},       // down left
                {-2, 1},        // left up
                {-2, -1},       // left down
        };

        for (int[] direction : directions) {
            int dr = direction[0];
            int dc = direction[1];

            int newRow = row + dr;
            int newCol = col + dc;

            if(ChessPosition.isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece newPositionPiece = board.getPiece(newPosition);

                if (newPositionPiece == null || newPositionPiece.getTeamColor() != knightColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }
}

class RookMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor bishopColor = board.getPiece(position).getTeamColor();
        int row = position.getRow();
        int col = position.getColumn();

        int[][] directions = {
                {0, 1},     // up
                {1, 0},    // right
                {0, -1},   // down
                {-1, 0}     // left
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

class PawnMoveCalculator implements ChessMoveCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessGame.TeamColor pawnColor = board.getPiece(position).getTeamColor();

        int r = (pawnColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pawnColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int newRow = position.getRow() + r;

        for (int c = -1; c <= 1; c++) {
            int newCol = position.getColumn() + c;
            boolean promotion = (newRow == 8 || newRow == 1);

            if (ChessPosition.isValidPosition(newRow, newCol)) {

                ChessPosition oneForward = new ChessPosition(newRow, newCol);
                ChessPiece oneForwardPiece = board.getPiece(oneForward);

                if (c == 0) {
                    if (oneForwardPiece == null) {
                        addPawnMoves(moves, position, oneForward, promotion);

                        if (position.getRow() == startRow && ChessPosition.isValidPosition(newRow + r, newCol)) {

                            ChessPosition twoForward = new ChessPosition(newRow + r, newCol);
                            ChessPiece twoForwardPiece = board.getPiece(twoForward);

                            if (twoForwardPiece == null) {
                                moves.add(new ChessMove(position, twoForward, null));
                            }
                        }
                    }
                } else {
                    if (oneForwardPiece != null && oneForwardPiece.getTeamColor() != pawnColor) {
                        addPawnMoves(moves, position, oneForward, promotion);
                    }
                }
            }
        }

        return moves;
    }

    private void addPawnMoves(Collection<ChessMove> moves, ChessPosition from, ChessPosition to, boolean promotion){
        if (promotion) {
            for (ChessPiece.PieceType type : ChessPiece.PROMOTION_PIECES) {
                moves.add(new ChessMove(from, to, type));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
