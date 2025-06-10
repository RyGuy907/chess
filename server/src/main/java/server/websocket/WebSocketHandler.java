package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.AuthService;
import service.GameService;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        UserGameCommand command = serializer.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connectHandler(command, session);
            case LEAVE -> leaveHandler(command, session);
            default -> sendHandler(session, new ErrorMessage("Error: invalid syntax"));
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
}
