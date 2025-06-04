package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import facade.ServerFacade;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {


    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() throws Exception {
        facade.clear();
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }

    @Test
    void registerValid() throws Exception {
        AuthData auth = facade.register("alice", "pass1", "alice@example.com");
        assertNotNull(auth);
    }

    @Test
    void registerInvalid() throws Exception {
        facade.register("alice", "pass1", "alice@example.com");
        assertThrows(Exception.class,
                () -> facade.register("alice", "pass2", "alice@example.com"));
    }

    @Test
    void loginValid() throws Exception {
        facade.register("bob", "pass2", "bob@example.com");
        AuthData auth = facade.login("bob", "pass2");
        assertNotNull(auth);
    }

    @Test
    void loginInvalid() throws Exception {
        facade.register("bob", "pass2", "bob@example.com");
        assertThrows(Exception.class,
                () -> facade.login("bob", "wrongpass"));
    }

    @Test
    void logoutValid() throws Exception {
        facade.register("carol", "pass3", "carol@example.com");
        AuthData auth = facade.login("carol", "pass3");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutInvalid() {
        assertThrows(Exception.class,
                () -> facade.logout("fake-token"));
    }

    @Test
    void createGameValid() throws Exception {
        facade.register("dave", "pass4", "dave@example.com");
        AuthData auth = facade.login("dave", "pass4");
        GameData game = facade.createGame("friendly‑match", auth.authToken());
        assertNotNull(game);
    }

    @Test
    void createGameInvalid() throws Exception {
        facade.register("dave", "pass4", "dave@example.com");
        AuthData auth = facade.login("dave", "pass4");
        assertThrows(Exception.class,
                () -> facade.createGame(null, auth.authToken()));
    }
    @Test
    void joinGameValid() throws Exception {
        facade.register("emma", "pass5", "emma@example.com");
        AuthData auth = facade.login("emma", "pass5");
        GameData created = facade.createGame("duel", auth.authToken());

        assertDoesNotThrow(() ->
                facade.joinGame(created.gameID(),
                        ChessGame.TeamColor.BLACK,
                        auth.authToken()));

        GameData[] games = facade.listGames(auth.authToken());
        GameData joined  = Arrays.stream(games)
                .filter(g -> g.gameID() == created.gameID())
                .findFirst()
                .orElseThrow();

        assertEquals("duel",  joined.gameName());
        assertEquals("emma",  joined.blackUsername());
    }

    @Test
    void joinGameInvalid() throws Exception {
        facade.register("frank", "pass6", "frank@example.com");
        AuthData auth1 = facade.login("frank", "pass6");

        facade.register("grace", "pass7", "grace@example.com");
        AuthData auth2 = facade.login("grace", "pass7");

        GameData created = facade.createGame("skirmish", auth1.authToken());
        facade.joinGame(created.gameID(),
                ChessGame.TeamColor.WHITE,
                auth1.authToken());

        assertThrows(Exception.class,
                () -> facade.joinGame(created.gameID(),
                        ChessGame.TeamColor.WHITE,
                        auth2.authToken()));
    }
    @Test
    void listGamesValid() throws Exception {
        facade.register("henry", "pass8", "henry@example.com");
        AuthData auth = facade.login("henry", "pass8");

        facade.createGame("g1", auth.authToken());
        facade.createGame("g2", auth.authToken());
        facade.createGame("g3", auth.authToken());

        GameData[] games = facade.listGames(auth.authToken());
        assertEquals(3, games.length);
    }

    @Test
    void listGamesInvalid() {
        assertThrows(Exception.class,
                () -> facade.listGames(null));
    }
}
