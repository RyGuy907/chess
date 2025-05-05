package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessPiece.PieceType.*;

public class PieceMovesCalculator {
    private final ChessBoard board;
    private final ChessPosition myPosition;
    private final ChessPiece.PieceType type;
    private int row;
    private int col;
    private Collection<ChessMove> moves = new ArrayList<>();

    public PieceMovesCalculator(ChessBoard board, ChessPosition myPosition, ChessPiece.PieceType type) {
        this.board = board;
        this.myPosition = myPosition;
        this.type = type;
        this.row = this.myPosition.getRow();
        this.col = this.myPosition.getColumn();
    }

    public Collection<ChessMove> findMoves() {
        return switch (type) {
            case KING -> kingMoveCalculator();
            case QUEEN -> queenMoveCalculator();
            case BISHOP -> bishopMoveCalculator();
            case KNIGHT -> knightMoveCalculator();
            case ROOK -> rookMoveCalculator();
            case PAWN -> pawnMoveCalculator();
        };
    }

    private Collection<ChessMove> kingMoveCalculator() {
        for(int x = row - 1; x <= row + 1; x++) {
            for(int y = col - 1; y <= col + 1; y++) {
                if (y == col && x == row) {
                    continue;
                }
                else if((x >= 9||y >= 9) || (x <= 0||y <= 0)) {
                    continue;
                }
                else {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessMove move = new ChessMove(myPosition, newSquare, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> queenMoveCalculator() {
        for(int x = row-1; x <= row+1; x++) {
            for(int y = col-1; y <= col+1; y++) {
                if (y == row && x == row) {
                    continue;
                }
                else if((x >= 8||y >= 8) || (x <= 0||y <=0)) {
                    continue;
                }
                else {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessMove move = new ChessMove(myPosition, newSquare, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> bishopMoveCalculator() {
        for(int x = row-1; x <= row+1; x++) {
            for(int y = col-1; y <= col+1; y++) {
                if (y == row && x == row) {
                    continue;
                }
                else if((x >= 8||y >= 8) || (x <= 0||y <=0)) {
                    continue;
                }
                else {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessMove move = new ChessMove(myPosition, newSquare, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> knightMoveCalculator() {
        for(int x = row-1; x <= row+1; x++) {
            for(int y = col-1; y <= col+1; y++) {
                if (y == row && x == row) {
                    continue;
                }
                else if((x >= 8||y >= 8) || (x <= 0||y <=0)) {
                    continue;
                }
                else {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessMove move = new ChessMove(myPosition, newSquare, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> rookMoveCalculator() {
        for(int x = row-1; x <= row+1; x++) {
            for(int y = col-1; y <= col+1; y++) {
                if (y == row && x == row) {
                    continue;
                }
                else if((x >= 8||y >= 8) || (x <= 0||y <=0)) {
                    continue;
                }
                else {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessMove move = new ChessMove(myPosition, newSquare, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
    private Collection<ChessMove> pawnMoveCalculator() {
        for(int x = row-1; x <= row+1; x++) {
            for(int y = col-1; y <= col+1; y++) {
                if (y == row && x == row) {
                    continue;
                }
                else if((x >= 8||y >= 8) || (x <= 0||y <=0)) {
                    continue;
                }
                else {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessMove move = new ChessMove(myPosition, newSquare, null);
                    moves.add(move);
                }
            }
        }
        return moves;
    }
}
