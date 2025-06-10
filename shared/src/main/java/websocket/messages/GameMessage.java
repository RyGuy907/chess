package websocket.messages;

public class GameMessage extends ServerMessage{
    public Object game;
    public GameMessage() { super(ServerMessageType.LOAD_GAME); }
    public GameMessage(Object game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }
}
