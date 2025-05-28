package dataaccess;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DAOFunctionsSQLTest {

    private DAOFunctionsSQL tester;

    @BeforeEach
    void resetDatabase() throws DataAccessException {
        tester = new DAOFunctionsSQL();
        tester.clear();
    }

    @Test
    void createUserSuccess() throws DataAccessException {
        UserData ben = new UserData("ben", "test", "test@mail");
        assertDoesNotThrow(() -> tester.createUser(ben));
        assertNotNull(tester.getUser("ben"));
    }

    @Test
    void createUserDuplicateFails() throws DataAccessException {
        UserData cam = new UserData("cam", "test2", "cam@mail");
        tester.createUser(cam);
        assertThrows(DataAccessException.class, () -> tester.createUser(cam));
    }

    @Test
    void getUserFound() throws DataAccessException {
        UserData dana = new UserData("dana", "test3", "dana@mail");
        tester.createUser(dana);
        UserData fetched = tester.getUser("dana");
        assertNotNull(fetched);
        assertEquals("dana", fetched.username());
    }

    @Test
    void getUserMissingReturnsNull() throws DataAccessException {
        assertNull(tester.getUser("false"));
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        tester.createUser(new UserData("erin", "test4", "erin@mail"));
        AuthData token = tester.createAuth("erin");
        assertNotNull(token.authToken());
        assertEquals("erin", token.username());
    }

    @Test
    void createAuthNullUserFails() {
        assertThrows(DataAccessException.class, () -> tester.createAuth(null));
    }

    @Test
    void getAuthFound() throws DataAccessException {
        tester.createUser(new UserData("frank", "test5", "frank@mail"));
        AuthData issued = tester.createAuth("frank");
        assertEquals(issued, tester.getAuth(issued.authToken()));
    }

    @Test
    void getAuthUnknownTokenReturnsNull() throws DataAccessException {
        assertNull(tester.getAuth(UUID.randomUUID().toString()));
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        tester.createUser(new UserData("gary", "test6", "gary@mail"));
        AuthData issued = tester.createAuth("gary");
        assertDoesNotThrow(() -> tester.deleteAuth(issued.authToken()));
        assertNull(tester.getAuth(issued.authToken()));
    }

    @Test
    void deleteAuthGracefullyIgnoresBadToken() throws DataAccessException {
        assertDoesNotThrow(() -> tester.deleteAuth(null));
        tester.createUser(new UserData("henry", "test7", "henry@mail"));
        AuthData issued = tester.createAuth("henry");
        assertDoesNotThrow(() -> tester.deleteAuth("not‑real"));
        assertNotNull(tester.getAuth(issued.authToken()));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        int id = tester.createGame(new GameData(0, null, null, "match1", null));
        assertTrue(id > 0);
    }

    @Test
    void createGameNoNameFails() {
        GameData nameless = new GameData(0, null, null, null, null);
        assertThrows(DataAccessException.class, () -> tester.createGame(nameless));
    }

    @Test
    void getGameFound() throws DataAccessException {
        int id = tester.createGame(new GameData(0, null, null, "match2", null));
        assertNotNull(tester.getGame(id));
    }

    @Test
    void getGameMissingReturnsNull() throws DataAccessException {
        assertNull(tester.getGame(424242));
    }

    @Test
    void listGamesReturnsAll() throws DataAccessException {
        tester.createGame(new GameData(0, null, null, "game1", null));
        tester.createGame(new GameData(0, null, null, "game2", null));
        assertEquals(2, tester.listGames().length);
    }

    @Test
    void listGamesEmptyReturnsZero() throws DataAccessException {
        assertEquals(0, tester.listGames().length);
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        int id = tester.createGame(new GameData(0, "white", null, "to‑edit", null));
        GameData edited = new GameData(id, "white", "black", "match3", new ChessGame());
        assertDoesNotThrow(() -> tester.updateGame(edited));
        assertEquals("match3", tester.getGame(id).gameName());
    }

    @Test
    void updateGameMissingNameFails() throws DataAccessException {
        int id = tester.createGame(new GameData(0, null, null, "temp", null));
        GameData bad = new GameData(id, null, null, null, null);
        assertThrows(DataAccessException.class, () -> tester.updateGame(bad));
    }

    @Test
    void clearEmptiesAllTables() throws DataAccessException {
        tester.createUser(new UserData("ivy", "test8", "test@mail"));
        tester.createAuth("ivy");
        tester.createGame(new GameData(0, null, null, "match4", null));
        tester.clear();
        assertNull(tester.getUser("ivy"));
        assertEquals(0, tester.listGames().length);
    }
}