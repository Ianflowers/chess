package chess;

import java.util.Collection;
import java.util.HashSet;

public interface ChessMovesCalculator {
    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos);
}


class calculateKingMoves implements ChessMovesCalculator {

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
                ChessPiece newPiece = board.getPiece(newPosition);

                if (newPiece != null && piece.getTeamColor() != newPiece.getTeamColor()) {
                    moves.add(new ChessMove(pos, newPosition, null));
                }
                if (newPiece == null) {
                    moves.add(new ChessMove(pos, newPosition, null));
                }
            }
        }

        return moves;
    }
}

class calculateQueenMoves implements ChessMovesCalculator {

    private final calculateBishopMoves bishopCalc = new calculateBishopMoves();
    private final calculateRookMoves rookCalc = new calculateRookMoves();

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();

        moves.addAll(bishopCalc.calculateMoves(board, pos));
        moves.addAll(rookCalc.calculateMoves(board, pos));

        return moves;
    }
}

class calculateBishopMoves implements ChessMovesCalculator {

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
                ChessPiece newPiece = board.getPiece(newPosition);

                if (newPiece != null && piece.getTeamColor() != newPiece.getTeamColor()) {
                    moves.add(new ChessMove(pos, newPosition, null));
                    break;
                }
                if (newPiece == null) {
                    moves.add(new ChessMove(pos, newPosition, null));
                }
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

class calculateKnightMoves implements ChessMovesCalculator {

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
                ChessPiece newPiece = board.getPiece(newPosition);

                if (newPiece != null && piece.getTeamColor() != newPiece.getTeamColor()) {
                    moves.add(new ChessMove(pos, newPosition, null));
                }
                if (newPiece == null) {
                    moves.add(new ChessMove(pos, newPosition, null));
                }
            }
        }

        return moves;
    }
}

class calculateRookMoves implements ChessMovesCalculator {

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
                ChessPiece newPiece = board.getPiece(newPosition);

                if (newPiece != null && piece.getTeamColor() != newPiece.getTeamColor()) {
                    moves.add(new ChessMove(pos, newPosition, null));
                    break;
                }
                if (newPiece == null) {
                    moves.add(new ChessMove(pos, newPosition, null));
                }
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

class calculatePawnMoves implements ChessMovesCalculator {

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

    void calculatePromotionPiece(Collection<ChessMove> moves, ChessPosition startPos, ChessPosition endPos){
        if (endPos.getRow() == 1 || endPos.getRow() == 8) {
            for (ChessPiece.PieceType type : ChessPiece.PROMOTION_PIECES) {
                moves.add(new ChessMove(startPos, endPos, type));
            }
        } else {
            moves.add(new ChessMove(startPos, endPos, null));
        }
    }



}