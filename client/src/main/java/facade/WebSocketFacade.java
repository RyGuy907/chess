package facade;

import chess.ChessMove;
import com.google.gson.Gson;
import ui.MessageHandler;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade extends Endpoint {
    private final Gson gson = new Gson();
    private final MessageHandler ui;
    private final String authToken;
    private final int gameID;
    private Session session;

    public WebSocketFacade(String baseUrl, MessageHandler ui, String authToken,
                          int gameID) throws Exception {
        this.ui = ui;
        this.authToken = authToken;
        this.gameID = gameID;
        URI uri = new URI(baseUrl.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, null));
        session.addMessageHandler(String.class, message -> {
            ServerMessage msg = gson.fromJson(message, ServerMessage.class);
            ui.notify(msg);
        });
    }

    public void makeMove(ChessMove move) {
        send(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move));
    }

    public void resign() {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, null));
    }

    public void leaveGame() {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, null));
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) session.close();
    }

    private void send(UserGameCommand cmd) {
        if (session == null || !session.isOpen()) return;
        session.getAsyncRemote().sendText(gson.toJson(cmd));
    }
}
