package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    private ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[(position.getRow()-1)][(position.getColumn()-1)] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (position.getRow() < 1 || position.getRow() > 8 || position.getColumn() < 1 || position.getColumn() > 8) {
            return null;
        }
        return squares[(position.getRow()-1)][(position.getColumn()-1)];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                squares[x][y] = null;
            }
        }
        for (int x = 0; x < 8; x++) {
            ChessPiece.PieceType piece;
            if (x == 0 || x == 7) {
                piece = ChessPiece.PieceType.ROOK;
            }
            else if (x == 1 || x == 6) {
                piece = ChessPiece.PieceType.KNIGHT;
            }
            else if (x == 2 || x == 5) {
                piece = ChessPiece.PieceType.BISHOP;
            }
            else if (x == 3)     {
                piece = ChessPiece.PieceType.QUEEN;
            }
            else {
                piece = ChessPiece.PieceType.KING;
            }
            squares[0][x] = new ChessPiece(ChessGame.TeamColor.WHITE, piece);
            squares[1][x] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            squares[6][x] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            squares[7][x] = new ChessPiece(ChessGame.TeamColor.BLACK, piece);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard boardCopy = (ChessBoard) super.clone();
            ChessPiece[][] newSquares = new ChessPiece[8][8];
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    ChessPiece piece = squares[x][y];
                    if (piece == null) {
                        newSquares[x][y] = null;
                    } else {
                        newSquares[x][y] = piece.clone();
                    }
                    }
            }
            boardCopy.squares = newSquares;
            return boardCopy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
