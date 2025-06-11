package server.websocket;

import chess.ChessGame;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@WebSocket
public class WebSocketHandler {
    private final ConcurrentMap<Integer, Set<Session>> gameRooms = new ConcurrentHashMap<>();
    private final AuthService authSvc = new AuthService();
    private final GameService gameSvc = new GameService();
    private final Gson json = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session wsSession) {
        System.out.println("WebSocket connected: " + wsSession);
    }

    @OnWebSocketMessage
    public void onMessage(Session wsSession, String payload) {
        try {
            UserGameCommand command = json.fromJson(payload, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT:
                    connectHandler(json.fromJson(payload, ConnectCommand.class), wsSession);
                    break;
                case LEAVE:
                    leaveHandler(json.fromJson(payload, LeaveCommand.class), wsSession);
                    break;
                case MAKE_MOVE:
                    moveHandler(json.fromJson(payload, MakeMoveCommand.class), wsSession);
                    break;
                case RESIGN:
                    resignHandler(json.fromJson(payload, ResignCommand.class), wsSession);
                    break;
                default:
                    errorHandler(wsSession, "Error: invalid syntax");
            }
        } catch (IOException | UnauthorizedException | DataAccessException e) {
            e.printStackTrace();
            errorHandler(wsSession, "Error: server problem");
        }
    }

    @OnWebSocketClose
    public void onClose(Session wsSession, int status, String reason) {
        gameRooms.values().forEach(sessions -> sessions.remove(wsSession));
    }

    @OnWebSocketError
    public void onError(Session wsSession, Throwable error) {
        gameRooms.values().forEach(sessions -> sessions.remove(wsSession));
    }

    private void connectHandler(ConnectCommand cmd, Session wsSession)
            throws IOException, DataAccessException, UnauthorizedException {
        String player = authSvc.getUsername(cmd.getAuthToken());
        GameData info;
        try {
            info = gameSvc.getGame(cmd.getGameID());
        } catch (DataAccessException e) {
            errorHandler(wsSession, "Error: invalid game ID");
            return;
        }
        if (info == null) {
            errorHandler(wsSession, "Error: invalid game ID");
            return;
        }
        ChessGame game = info.game();
        Set<Session> room = gameRooms.computeIfAbsent(cmd.getGameID(), k -> ConcurrentHashMap.newKeySet());
        String role = determineRole(player, info);
        String joinedMessage = player + " joined as " + role;
        for (Session peer : room) {
            notificationHandler(peer, joinedMessage);
        }
        room.add(wsSession);
        loadGameHandler(wsSession, game);
    }

    private void leaveHandler(LeaveCommand cmd, Session wsSession)
            throws IOException, DataAccessException, UnauthorizedException {
        String player = authSvc.getUsername(cmd.getAuthToken());
        gameSvc.leaveGame(player, cmd.getGameID());

        Set<Session> room = gameRooms.get(cmd.getGameID());
        if (room != null) {
            room.remove(wsSession);
            String leaveMsg = player + " left the game";
            for (Session peer : room) {
                notificationHandler(peer, leaveMsg);
            }
        }
        wsSession.close();
    }

    private void moveHandler(MakeMoveCommand cmd, Session wsSession)
            throws IOException, DataAccessException, UnauthorizedException {
        String player = authSvc.getUsername(cmd.getAuthToken());
        GameData info = gameSvc.getGame(cmd.getGameID());
        ChessGame game = info.game();
        ChessPiece piece = game.getBoard().getPiece(cmd.getMove().getStartPosition());
        ChessGame.TeamColor turn = game.getTeamTurn();
        boolean whiteToMove = player.equals(info.whiteUsername());
        boolean blackToMove = player.equals(info.blackUsername());

        if (game.gameOver()) {
            errorHandler(wsSession, "Error: game over");
            return;
        }
        if ((turn == ChessGame.TeamColor.WHITE && !whiteToMove) || (turn == ChessGame.TeamColor.BLACK && !blackToMove)) {
            errorHandler(wsSession, "Error: not your turn");
            return;
        }
        if (piece == null || (piece.getTeamColor() == ChessGame.TeamColor.WHITE && !whiteToMove)
                || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && !blackToMove)) {
            errorHandler(wsSession, "Error: invalid piece selection");
            return;
        }
        try {
            gameSvc.makeMove(cmd.getGameID(), cmd.getMove());
        } catch (InvalidMoveException e) {
            errorHandler(wsSession, "Error: invalid move");
            return;
        }
        GameData updated = gameSvc.getGame(cmd.getGameID());
        ChessGame board = updated.game();

        Set<Session> room = gameRooms.get(cmd.getGameID());
        if (room != null) {
            for (Session peer : room) {
                loadGameHandler(peer, board);
                if (!peer.equals(wsSession)) {
                    notificationHandler(peer, player + " moved");
                }
            }
        }
        updateGameStatus(updated);
    }

    private void resignHandler(ResignCommand cmd, Session wsSession)
            throws IOException, DataAccessException, UnauthorizedException {
        String player = authSvc.getUsername(cmd.getAuthToken());
        GameData info = gameSvc.getGame(cmd.getGameID());
        ChessGame game = info.game();
        boolean white = player.equals(info.whiteUsername());
        boolean black = player.equals(info.blackUsername());

        if (!white && !black) {
            errorHandler(wsSession, "Error: only players can resign");
            return;
        }
        if (game.gameOver()) {
            errorHandler(wsSession, "Error: game already over");
            return;
        }
        gameSvc.resign(cmd.getGameID(), player);
        Set<Session> room = gameRooms.get(cmd.getGameID());
        if (room != null) {
            for (Session peer : room) {
                notificationHandler(peer, player + " resigned");
            }
        }
    }

    private void updateGameStatus(GameData info) {
        ChessGame game = info.game();
        ChessGame.TeamColor next = game.getTeamTurn();
        String player = null;
        if (next == ChessGame.TeamColor.WHITE) {
            player = info.whiteUsername();
        } else {
            player = info.blackUsername();
        }
        String message = null;
        if (game.isInCheckmate(next)) {
            message = player + " is in checkmate";
            game.setGameOver(true);
        } else if (game.isInCheck(next)) {
            message = player + " is in check";
        } else if (game.isInStalemate(next)) {
            message = player + " is in stalemate";
            game.setGameOver(true);
        }
        if (message != null) {
            for (Session s : gameRooms.get(info.gameID())) {
                notificationHandler(s, message);
            }
        }
        try {
            gameSvc.updateGame(new GameData(
                    info.gameID(), info.whiteUsername(), info.blackUsername(), info.gameName(), game
            ));
        } catch (DataAccessException ignored) {}
    }

    private void loadGameHandler(Session wsSession, ChessGame game) {
        try {
            wsSession.getRemote().sendString(json.toJson(new LoadGameMessage(game)));
        } catch (IOException ignored) {}
    }

    private void notificationHandler(Session wsSession, String note) {
        try {
            wsSession.getRemote().sendString(json.toJson(new NotificationMessage(note)));
        } catch (IOException ignored) {}
    }

    private void errorHandler(Session wsSession, String error) {
        try {
            wsSession.getRemote().sendString(json.toJson(new ErrorMessage(error)));
        } catch (IOException ignored) {}
    }

    private String determineRole(String player, GameData info) {
        if (player.equals(info.whiteUsername())) {
            return "white";
        } else if (player.equals(info.blackUsername())) {
            return "black";
        } else {
            return "observer";
        }
    }
}
