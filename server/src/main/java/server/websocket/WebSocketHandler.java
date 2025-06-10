package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.AuthService;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.GameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@WebSocket
public class WebSocketHandler {

    private final Map<String, Session> sessionsToken = new ConcurrentHashMap<>();
    private final Map<String, Integer> gameToken = new ConcurrentHashMap<>();
    private final Map<Integer, Set<String> >tokensGame = new ConcurrentHashMap<>();
    private final AuthService authService = new AuthService();
    private final GameService gameService = new GameService();
    private final Gson serializer = new Gson();



    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        try {
            UserGameCommand command = serializer.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connectHandler(command, session);
                case LEAVE -> leaveHandler(command, session);
                default -> send(session, new ErrorMessage("Error: invalid syntax"));
            }
        } catch (DataAccessException | IOException | UnauthorizedException exception) {
            exception.printStackTrace();
            send(session, new ErrorMessage("Error: server problem"));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private void connectHandler(UserGameCommand command, Session session) throws IOException, DataAccessException, UnauthorizedException {
        String user = authService.getUsername(command.getAuthToken());
        GameData game = gameService.getGame(command.getGameID());
        if (user == null || game == null) {
            send(session, new ErrorMessage("Error: invalid authToken or gameID"));
            return;
        }
        sessionsToken.put(command.getAuthToken(), session);
        gameToken.put(command.getAuthToken(), command.getGameID());
        tokensGame.computeIfAbsent(command.getGameID(), k -> ConcurrentHashMap.newKeySet())
                .add(command.getAuthToken());
        send(session, new GameMessage(game));
        String role = getRole(user, game);
        sendOut(command.getGameID(), command.getAuthToken(), new NotificationMessage(user + " joined as " + role));
    }

    private void send(Session session, ServerMessage message) throws IOException {
        if (session.isOpen()) session.getRemote().sendString(serializer.toJson(message));
    }
    private void sendOut(int gameID, String excludeToken, ServerMessage message) {
        String json = serializer.toJson(message);
        List<String> toRemove = new ArrayList<>();
        sessionsToken.forEach((token, session) -> {
            if (!session.isOpen()) {
                toRemove.add(token);
                return;
            }
            Integer game = gameToken.get(token);
            if (game != null && game == gameID && !Objects.equals(token, excludeToken)) {
                try { session.getRemote().sendString(json); }
                catch (IOException ignored) {}
            }
        });
        for (String token : toRemove) {
            sessionsToken.remove(token);
            Integer game = gameToken.remove(token);
            if (game != null) {
                Set<String> set = tokensGame.get(game);
                if (set != null) set.remove(token);
            }
        }
    }
    private String getRole(String user, GameData g) {
        if (user.equals(g.whiteUsername())) {
            return "white";
        }
        if (user.equals(g.blackUsername())) {
            return "black";
        }
        return "observer";
    }
}
