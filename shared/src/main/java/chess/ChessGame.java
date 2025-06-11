package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 *
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board = new ChessBoard();
    private TeamColor currentTurn = TeamColor.WHITE;
    private boolean gameOver = false;

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public boolean gameOver() {
        return gameOver;
    }

    public void resign(String username, String winner) {
        this.gameOver = true;
    }

    public void setGameOver(boolean value) {
        gameOver = value;
    }

    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    private void switchTurn() {
        currentTurn = enemyColor(currentTurn);
    }

    public TeamColor enemyColor(TeamColor color) {
        return color == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor { WHITE, BLACK }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece startPiece = board.getPiece(startPosition);
        if (startPiece == null) {
            return null;
        }

        Collection<ChessMove> validMovesList = new ArrayList<>();
        for (ChessMove candidateMove : startPiece.pieceMoves(board, startPosition)) {
            ChessBoard boardCopy = board.clone();
            doMove(boardCopy, candidateMove);
            ChessPosition kingPosition = getKingPosition(boardCopy, startPiece.getTeamColor());
            boolean kingInCheck = kingPosition == null
                    || inDanger(boardCopy, kingPosition, enemyColor(startPiece.getTeamColor()));
            if (!kingInCheck) {
                validMovesList.add(candidateMove);
            }
        }
        return validMovesList;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece startPiece = board.getPiece(move.getStartPosition());
        if (startPiece == null
                || startPiece.getTeamColor() != currentTurn
                || !validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException();
        }
        doMove(board, move);
        switchTurn();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessGame otherGame = (ChessGame) obj;
        return Objects.equals(board, otherGame.board) && currentTurn == otherGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    // in check method
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(board, teamColor);
        return kingPosition == null || inDanger(board, kingPosition, enemyColor(teamColor));
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    // in checkmate method
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return noValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    // in stalemate method
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    // set board method
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    // get board method
    public ChessBoard getBoard() {
        return board;
    }

    private void doMove(ChessBoard board, ChessMove move) {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), movingPiece);
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(),
                    new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece()));
        }
    }

    private boolean inDanger(ChessBoard board, ChessPosition square, TeamColor enemyColor) {
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition fromPosition = new ChessPosition(x, y);
                ChessPiece piece = board.getPiece(fromPosition);
                if (piece == null || piece.getTeamColor() != enemyColor) {
                    continue;
                }
                if (attacksSquare(board, fromPosition, square)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attacksSquare(ChessBoard board, ChessPosition from, ChessPosition target) {
        ChessPiece attackingPiece = board.getPiece(from);
        if (attackingPiece == null) {
            return false;
        }
        for (ChessMove attackMove : attackingPiece.pieceMoves(board, from)) {
            if (attackMove.getEndPosition().equals(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean noValidMoves(TeamColor teamColor) {
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition position = new ChessPosition(x, y);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private ChessPosition getKingPosition(ChessBoard board, TeamColor color) {
        for (int x = 1; x <= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPiece piece = board.getPiece(new ChessPosition(x, y));
                if (piece != null
                        && piece.getTeamColor() == color
                        && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(x, y);
                }
            }
        }
        return null;
    }
}
