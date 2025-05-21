package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.TeamColor.WHITE;
import static chess.ChessPiece.PieceType.*;
import static java.lang.Math.abs;

public class PieceMovesCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    private final ChessPiece.PieceType type;
    private final ChessGame.TeamColor pieceColor;
    private int row;
    private int col;
    private Collection<ChessMove> moves = new ArrayList<>();

    public PieceMovesCalculator(ChessBoard board,
                                ChessPosition myPosition,
                                ChessPiece.PieceType type,
                                ChessGame.TeamColor pieceColor) {
        this.board = board;
        this.myPosition = myPosition;
        this.type = type;
        this.pieceColor = pieceColor;
        this.row = this.myPosition.getRow();
        this.col = this.myPosition.getColumn();
    }

    public Collection<ChessMove> findMoves() {
        return switch (type) {
            case KING   -> kingMoveCalculator();
            case QUEEN  -> queenMoveCalculator();
            case BISHOP -> bishopMoveCalculator();
            case KNIGHT -> knightMoveCalculator();
            case ROOK   -> rookMoveCalculator();
            case PAWN   -> pawnMoveCalculator();
        };
    }

    private Collection<ChessMove> stepMoveCalculator(int[][] directions) {
        for (int[] dir : directions) {
            int x = row + dir[0];
            int y = col + dir[1];
            if (!(x >= 1 && x <= 8 && y >= 1 && y <= 8)) {
                continue;
            }
            checkAndAdd(x, y);
        }
        return moves;
    }

    private Collection<ChessMove> kingMoveCalculator() {
        int[][] kingDirections = {
                {0, +1}, {+1, +1}, {+1, 0}, {+1, -1},
                {0, -1}, {-1, -1}, {-1, 0}, {-1, +1}
        };
        return stepMoveCalculator(kingDirections);
    }

    private Collection<ChessMove> slideMoveCalculator(int[][] directions) {
        for (int[] n : directions) {
            int x = row + n[0];
            int y = col + n[1];
            while (x >= 1 && x <= 8 && y >= 1 && y <= 8) {
                int flag = checkAndAdd(x, y);
                if (flag == 1) {
                    break;
                }
                x += n[0];
                y += n[1];
            }
        }
        return moves;
    }

    private Collection<ChessMove> queenMoveCalculator() {
        int[][] queenDirections = {
                {0, +1}, {+1, +1}, {+1, 0}, {+1, -1},
                {0, -1}, {-1, -1}, {-1, 0}, {-1, +1}
        };
        return slideMoveCalculator(queenDirections);
    }

    private Collection<ChessMove> bishopMoveCalculator() {
        int[][] bishopDirections = {
                {+1, +1}, {+1, -1}, {-1, -1}, {-1, +1}
        };
        return slideMoveCalculator(bishopDirections);
    }

    private Collection<ChessMove> knightMoveCalculator() {
        int[][] knightDirections = {
                {+2, +1}, {+1, +2}, {-1, +2}, {-2, +1},
                {-2, -1}, {-1, -2}, {+1, -2}, {+2, -1}
        };
        return stepMoveCalculator(knightDirections);
    }

    private Collection<ChessMove> rookMoveCalculator() {
        int[][] rookDirections = {
                {+1, 0}, {0, +1}, {-1, 0}, {0, -1}
        };
        return slideMoveCalculator(rookDirections);
    }

    private Collection<ChessMove> pawnMoveCalculator() {
        moves.clear();
        int[][] pawnDirections = {
                {+1, 0}, {+2, 0}, {+1, +1}, {+1, -1}
        };
        for (int[] n : pawnDirections) {
            int x;
            if (pieceColor == WHITE) {
                if ((row != 2) && (n[0] == 2)) {
                    continue;
                }
                x = row + n[0];
            } else {
                if ((row != 7) && (n[0] == 2)) {
                    continue;
                }
                x = row - n[0];
            }
            int y = col + n[1];
            if ((x < 1 || y < 1) || (x > 8 || y > 8)) {
                continue;
            }
            if (n[0] == 2 && n[1] == 0) {
                int var = (pieceColor == WHITE) ? row + 1 : row - 1;
                if (board.getPiece(new ChessPosition(var, col)) != null) {
                    continue;
                }
            }
            ChessPosition newSquare = new ChessPosition(x, y);
            ChessPiece pieceCheck = board.getPiece(newSquare);
            if ((abs(n[1]) == 1) && (pieceCheck == null)) {
                continue;
            } else if ((n[1] == 0) && (pieceCheck != null)) {
                continue;
            }
            checkAndAdd(x, y);
        }
        return moves;
    }

    private int checkAndAdd(int x, int y) {
        ChessPosition newSquare = new ChessPosition(x, y);
        ChessPiece pieceCheck = board.getPiece(newSquare);
        if (pieceCheck != null) {
            if (pieceCheck.getTeamColor() == pieceColor) {
                return 1;
            } else {
                return doAdd(x, y);
            }
        }
        if (type == PAWN && (x == 8 || x == 1)) {
            return doAdd(x, y);
        }
        ChessMove move = new ChessMove(myPosition, newSquare, null);
        moves.add(move);
        return 0;
    }

    private int doAdd(int x, int y) {
        if ((type == PAWN) && (x == 8 || x == 1)) {
            ChessPosition newSquare = new ChessPosition(x, y);
            moves.add(new ChessMove(myPosition, newSquare, QUEEN));
            moves.add(new ChessMove(myPosition, newSquare, BISHOP));
            moves.add(new ChessMove(myPosition, newSquare, ROOK));
            moves.add(new ChessMove(myPosition, newSquare, KNIGHT));
            return 1;
        } else {
            ChessPosition newSquare = new ChessPosition(x, y);
            ChessMove move = new ChessMove(myPosition, newSquare, null);
            moves.add(move);
            return 1;
        }
    }
}
