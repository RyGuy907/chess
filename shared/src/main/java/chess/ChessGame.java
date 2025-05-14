package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board1 = new ChessBoard();
    private TeamColor turn = TeamColor.WHITE;
    public ChessGame() {
        board1.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoveList = new ArrayList<>();
        ChessPiece piece = board1.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board1, startPosition);
        for (ChessMove move : moves) {
            ChessBoard boardCopy = board1.clone();
            ChessPiece piece1 = boardCopy.getPiece(move.getStartPosition());
            boardCopy.addPiece(move.getStartPosition(), null);
            boardCopy.addPiece(move.getEndPosition(), piece1);
            ChessPiece promotePiece = new ChessPiece(piece1.getTeamColor(), move.getPromotionPiece());
            if (move.getPromotionPiece() != null) {
                boardCopy.addPiece(move.getEndPosition(), promotePiece);
            }
            ChessPosition king = null;
            boolean inCheck = false;
            for (int x = 1; x <= 8 && king == null; x++) {
                for (int y = 1; y <= 8; y++) {
                    ChessPiece newPiece = boardCopy.getPiece(new ChessPosition(x, y));
                    if ((newPiece != null) && (newPiece.getTeamColor() == piece.getTeamColor()) && (newPiece.getPieceType() == ChessPiece.PieceType.KING)) {
                        king = new ChessPosition(x, y);
                        break;
                    }
                    }
            }
            for (int x = 1; x <= 8 && !inCheck; x++) {
                for (int y = 1; y <= 8 && !inCheck; y++) {
                    ChessPosition newSquare = new ChessPosition(x, y);
                    ChessPiece pieceCheck = boardCopy.getPiece(newSquare);
                    if ((pieceCheck != null) && (pieceCheck.getTeamColor() != piece.getTeamColor())) {
                        for (ChessMove enemymove : pieceCheck.pieceMoves(boardCopy, newSquare)) {
                            if (enemymove.getEndPosition().equals(king)) {
                                inCheck = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!inCheck) validMoveList.add(move);
        }
        return validMoveList;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> allowedMoves = validMoves(move.getStartPosition());
        ChessPiece piece = board1.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException();
        }
        else if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException();
        }
        else if (allowedMoves == null || !allowedMoves.contains(move)) {
            throw new InvalidMoveException();
        }
        board1.addPiece(move.getStartPosition(), null);
        board1.addPiece(move.getEndPosition(), piece);
        if (move.getPromotionPiece() != null) {
            ChessPiece promotePiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board1.addPiece(move.getEndPosition(), promotePiece);
        }
        if (turn == TeamColor.WHITE) {
            turn = TeamColor.BLACK;
        } else {
            turn = TeamColor.WHITE;
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
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board1, chessGame.board1) && turn == chessGame.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board1, turn);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition king = null;
        for (int x = 1; x <= 8; x++) {
            if (king != null) {
                break;
            }
            for (int y = 1; y <= 8; y++) {
                ChessPiece piece = board1.getPiece(new ChessPosition(x, y));
                if ((piece != null) && (piece.getTeamColor() == teamColor) && (piece.getPieceType() == ChessPiece.PieceType.KING)) {
                    king = new ChessPosition(x, y);
                    break;
                }
            }
        }
        if (king == null) {
            return true;
        }
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition newSquare = new ChessPosition(x, y);
                ChessPiece pieceCheck = board1.getPiece(newSquare);
                if ((pieceCheck != null) && (pieceCheck.getTeamColor() != teamColor)) {
                    for (ChessMove move : pieceCheck.pieceMoves(board1, newSquare)) {
                        if (move.getEndPosition().equals(king)) {
                            return true;
                        }
                    }
                }
                }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition newSquare = new ChessPosition(x, y);
                ChessPiece pieceCheck = board1.getPiece(newSquare);
                if ((pieceCheck != null) && (pieceCheck.getTeamColor() == teamColor)) {
                    Collection<ChessMove> moves = validMoves(newSquare);
                    if ((moves != null) && (!moves.isEmpty())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition newSquare = new ChessPosition(x, y);
                ChessPiece pieceCheck = board1.getPiece(newSquare);
                if ((pieceCheck != null) && (pieceCheck.getTeamColor() == teamColor)) {
                    Collection<ChessMove> moves = validMoves(newSquare);
                    if ((moves != null) && (!moves.isEmpty())) {
                        return false;
                    }
                }
                }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        board1 = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board1;
    }
}
