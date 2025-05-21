package service;

import dataaccess.*;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setup() {
        DAO.clear();
        gameService = new GameService();
        authService = new AuthService();
    }

    @Test
    void createGameValid() throws Exception {
        UserData user = new UserData("bob", "test", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        int id = gameService.createGame(token, "board");
        GameData game = DAO.getGame(id);
        assertEquals("board", game.gameName());
    }

    @Test
    void createGameInvalid() throws Exception {
        UserData user = new UserData("joe", "test1", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        assertThrows(BadRequestException.class, () -> gameService.createGame(token, null));
    }

    @Test
    void listGames_positive() throws Exception {
        UserData user = new UserData("steve", "test2", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        assertEquals(0, gameService.listGames(token).length);
    }

    @Test
    void listGames_unauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("wrong"));
    }

    @Test
    void joinGame_positive() throws Exception {
        UserData user = new UserData("mark", "test3", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        int id = gameService.createGame(token, "board");
        gameService.joinGame(token, "WHITE", id);
        assertEquals("mark", DAO.getGame(id).whiteUsername());
    }

    @Test
    void joinGame_alreadyTaken() throws Exception {
        UserData one = new UserData("jim", "test4", null);
        UserData two = new UserData("jones", "test5", null);
        DAO.createUser(one);
        DAO.createUser(two);
        String token1 = authService.loginData(one).authToken();
        String token2 = authService.loginData(two).authToken();
        int id = gameService.createGame(token1, "board");
        gameService.joinGame(token1, "BLACK", id);
        assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(token2, "BLACK", id));
    }

    @Test
    void clearCheck() throws DataAccessException {
        DAO.createGame(new GameData(0, null, null, "new", null));
        gameService.clear();
        assertEquals(0, DAO.listGames().length);
    }
}
