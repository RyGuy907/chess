package dataaccess;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DAOFunctionsSQLTest {

    private DAOFunctionsSQL sut;

    @BeforeEach
    void resetDatabase() throws DataAccessException {
        sut = new DAOFunctionsSQL();
        sut.clear();
    }

    @Test
    void createUserSuccess() throws DataAccessException {
        UserData ben = new UserData("ben", "test", "test@mail");
        assertDoesNotThrow(() -> sut.createUser(ben));
        assertNotNull(sut.getUser("ben"));
    }

    @Test
    void createUserDuplicateFails() throws DataAccessException {
        UserData cam = new UserData("cam", "test2", "cam@mail");
        sut.createUser(cam);
        assertThrows(DataAccessException.class, () -> sut.createUser(cam));
    }

    @Test
    void getUserFound() throws DataAccessException {
        UserData dana = new UserData("dana", "test3", "dana@mail");
        sut.createUser(dana);
        UserData fetched = sut.getUser("dana");
        assertNotNull(fetched);
        assertEquals("dana", fetched.username());
    }

    @Test
    void getUserMissingReturnsNull() throws DataAccessException {
        assertNull(sut.getUser("false"));
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        sut.createUser(new UserData("erin", "test4", "erin@mail"));
        AuthData token = sut.createAuth("erin");
        assertNotNull(token.authToken());
        assertEquals("erin", token.username());
    }

    @Test
    void createAuthNullUserFails() {
        assertThrows(DataAccessException.class, () -> sut.createAuth(null));
    }

    @Test
    void getAuthFound() throws DataAccessException {
        sut.createUser(new UserData("frank", "test5", "frank@mail"));
        AuthData issued = sut.createAuth("frank");
        assertEquals(issued, sut.getAuth(issued.authToken()));
    }

    @Test
    void getAuthUnknownTokenReturnsNull() throws DataAccessException {
        assertNull(sut.getAuth(UUID.randomUUID().toString()));
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        sut.createUser(new UserData("gary", "test6", "gary@mail"));
        AuthData issued = sut.createAuth("gary");
        assertDoesNotThrow(() -> sut.deleteAuth(issued.authToken()));
        assertNull(sut.getAuth(issued.authToken()));
    }

    @Test
    void deleteAuthGracefullyIgnoresBadToken() throws DataAccessException {
        assertDoesNotThrow(() -> sut.deleteAuth(null));
        sut.createUser(new UserData("henry", "test7", "henry@mail"));
        AuthData issued = sut.createAuth("henry");
        assertDoesNotThrow(() -> sut.deleteAuth("not‑real"));
        assertNotNull(sut.getAuth(issued.authToken()));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        int id = sut.createGame(new GameData(0, null, null, "match1", null));
        assertTrue(id > 0);
    }

    @Test
    void createGameNoNameFails() {
        GameData nameless = new GameData(0, null, null, null, null);
        assertThrows(DataAccessException.class, () -> sut.createGame(nameless));
    }

    @Test
    void getGameFound() throws DataAccessException {
        int id = sut.createGame(new GameData(0, null, null, "match2", null));
        assertNotNull(sut.getGame(id));
    }

    @Test
    void getGameMissingReturnsNull() throws DataAccessException {
        assertNull(sut.getGame(424242));
    }

    @Test
    void listGamesReturnsAll() throws DataAccessException {
        sut.createGame(new GameData(0, null, null, "game1", null));
        sut.createGame(new GameData(0, null, null, "game2", null));
        assertEquals(2, sut.listGames().length);
    }

    @Test
    void listGamesEmptyReturnsZero() throws DataAccessException {
        assertEquals(0, sut.listGames().length);
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        int id = sut.createGame(new GameData(0, "white", null, "to‑edit", null));
        GameData edited = new GameData(id, "white", "black", "match3", new ChessGame());
        assertDoesNotThrow(() -> sut.updateGame(edited));
        assertEquals("match3", sut.getGame(id).gameName());
    }

    @Test
    void updateGameMissingNameFails() throws DataAccessException {
        int id = sut.createGame(new GameData(0, null, null, "temp", null));
        GameData bad = new GameData(id, null, null, null, null);
        assertThrows(DataAccessException.class, () -> sut.updateGame(bad));
    }

    @Test
    void clearEmptiesAllTables() throws DataAccessException {
        sut.createUser(new UserData("ivy", "test8", "test@mail"));
        sut.createAuth("ivy");
        sut.createGame(new GameData(0, null, null, "match4", null));
        sut.clear();
        assertNull(sut.getUser("ivy"));
        assertEquals(0, sut.listGames().length);
    }
}