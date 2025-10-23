package chess;

import java.util.Collection;
import java.util.HashSet;

public interface ChessMovesCalculator {
    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos);

    default boolean addValidMoveIfPossible(ChessBoard board, ChessPosition currentPos, ChessPosition newPos, ChessPiece piece, Collection<ChessMove> moves) {
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

        int[][] directions = {
                {0, 1},
                {1, 1},
                {1, 0},
                {1, -1},
                {0, -1},
                {-1, -1},
                {-1, 0},
                {-1, 1},
        };

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
        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece piece = board.getPiece(pos);

        int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, -1},
                {-1, 1},
        };

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
}

class CalculateKnightMoves implements ChessMovesCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece piece = board.getPiece(pos);

        int[][] directions = {
                {1, 2},
                {2, 1},
                {2, -1},
                {1, -2},
                {-1, -2},
                {-2, -1},
                {-2, 1},
                {-1, 2},
        };

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
}

class CalculateRookMoves implements ChessMovesCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece piece = board.getPiece(pos);

        int[][] directions = {
                {0, 1},
                {1, 0},
                {0, -1},
                {-1, 0},
        };

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
}

class CalculatePawnMoves implements ChessMovesCalculator {

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece piece = board.getPiece(pos);
        ChessGame.TeamColor color = piece.getTeamColor();
        int row = pos.getRow();
        int col = pos.getColumn();

        boolean firstMove = row == 2 && color == ChessGame.TeamColor.WHITE || row == 7 && color == ChessGame.TeamColor.BLACK;
        int r = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;


        for (int c = -1; c <= 1; c++) {
            int dx = row + r;
            int dy = col + c;

            if (ChessBoard.isValidPosition(dx, dy)) {

                ChessPosition forwardOne = new ChessPosition(dx, dy);
                ChessPiece forwardOnePiece = board.getPiece(forwardOne);


                if (c == 0) {
                    if (forwardOnePiece == null) {
                        calculatePromotionPiece(moves, pos, forwardOne);

                        if (firstMove && ChessBoard.isValidPosition(dx + r, dy)) {
                            ChessPosition forwardTwo = new ChessPosition(dx + r, dy);
                            ChessPiece forwardTwoPiece = board.getPiece(forwardTwo);

                            if (forwardTwoPiece == null) {
                                moves.add(new ChessMove(pos, forwardTwo, null));
                            }
                        }
                    }

                } else {
                    if (forwardOnePiece != null && piece.getTeamColor() != forwardOnePiece.getTeamColor()) {
                        calculatePromotionPiece(moves, pos, forwardOne);
                    }
                }
            }
        }

        return moves;
    }

    void calculatePromotionPiece(Collection<ChessMove> moves, ChessPosition startPos, ChessPosition endPos) {
        if (endPos.getRow() == 1 || endPos.getRow() == 8) {
            for (ChessPiece.PieceType type : ChessPiece.PROMOTION_PIECES) {
                moves.add(new ChessMove(startPos, endPos, type));
            }
        } else {
            moves.add(new ChessMove(startPos, endPos, null));
        }
    }
}


