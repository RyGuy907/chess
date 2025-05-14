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
        Collection<ChessMove> legalMoves = new ArrayList<>();
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
            if (move.getPromotionPiece() != null) {
                boardCopy.addPiece(
                        move.getEndPosition(),
                        new ChessPiece(piece1.getTeamColor(), move.getPromotionPiece()));
            }
            ChessPosition kingPos = null;
            for (int r = 1; r <= 8 && kingPos == null; r++) {
                for (int c = 1; c <= 8; c++) {
                    ChessPiece p = boardCopy.getPiece(new ChessPosition(r, c));
                    if (p != null &&
                            p.getTeamColor() == piece.getTeamColor() &&
                            p.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPos = new ChessPosition(r, c);
                        break;
                    }
                }
            }
            boolean kingInCheck = false;
            for (int r = 1; r <= 8 && !kingInCheck; r++) {
                for (int c = 1; c <= 8 && !kingInCheck; c++) {
                    ChessPosition pos = new ChessPosition(r, c);
                    ChessPiece enemy = boardCopy.getPiece(pos);
                    if (enemy != null && enemy.getTeamColor() != piece.getTeamColor()) {
                        for (ChessMove em : enemy.pieceMoves(boardCopy, pos)) {
                            if (em.getEndPosition().equals(kingPos)) {
                                kingInCheck = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!kingInCheck) legalMoves.add(move);
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board1.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece on the starting square.");
        }
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException("It is not " + turn + "'s turn.");
        }
        Collection<ChessMove> allowed = validMoves(move.getStartPosition());
        if (allowed == null || !allowed.contains(move)) {
            throw new InvalidMoveException("Illegal move.");
        }
        board1.addPiece(move.getStartPosition(), null);
        board1.addPiece(move.getEndPosition(), piece);

        if (move.getPromotionPiece() != null) {
            board1.addPiece(
                    move.getEndPosition(),
                    new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        turn = (turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
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
        ChessPosition kingPos = null;
        for (int x = 1; x <= 8 && kingPos == null; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPiece p = board1.getPiece(new ChessPosition(x, y));
                if (p != null &&
                        p.getTeamColor() == teamColor &&
                        p.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = new ChessPosition(x, y);
                    break;
                }
            }
        }
        if (kingPos == null) return true;
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition pos = new ChessPosition(x, y);
                ChessPiece enemy = board1.getPiece(pos);
                if (enemy != null && enemy.getTeamColor() != teamColor) {
                    for (ChessMove m : enemy.pieceMoves(board1, pos)) {
                        if (m.getEndPosition().equals(kingPos)) return true;
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
        if (!isInCheck(teamColor)) return false;

        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition pos = new ChessPosition(x, y);
                ChessPiece p = board1.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor) {
                    Collection<ChessMove> vm = validMoves(pos);
                    if (vm != null && !vm.isEmpty()) return false;
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
        if (isInCheck(teamColor)) return false;

        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition pos = new ChessPosition(x, y);
                ChessPiece p = board1.getPiece(pos);
                if (p != null && p.getTeamColor() == teamColor) {
                    Collection<ChessMove> vm = validMoves(pos);
                    if (vm != null && !vm.isEmpty()) return false;
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
