package service;

import dataaccess.DAO;
import dataaccess.UnauthorizedException;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setup() throws DataAccessException {
        DAO.clear();
        authService = new AuthService();
    }

    @Test
    void loginDataValid() throws DataAccessException {
        UserData user = new UserData("bob", "test", null);
        DAO.createUser(user);
        AuthData auth = authService.loginData(user);
        assertNotNull(auth.authToken());
        assertEquals("bob", auth.username());
        assertNotNull(DAO.getAuth(auth.authToken()));
    }

    @Test
    void loginDataMissing() throws DataAccessException {
        UserData user = new UserData("joe", "test1", null);
        AuthData auth = authService.loginData(user);
        assertNotNull(auth.authToken());
        assertEquals("joe", auth.username());
    }

    @Test
    void logoutDataValid() throws UnauthorizedException, DataAccessException {
        UserData user = new UserData("steve", "test3", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        authService.logoutData(token);
        assertNull(DAO.getAuth(token));
    }

    @Test
    void logoutDataBadToken() {
        assertThrows(UnauthorizedException.class,
                () -> authService.logoutData("wrong"));
    }

    @Test
    void validateTokenValid() throws UnauthorizedException, DataAccessException {
        UserData user = new UserData("mark", "test4", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        authService.validateToken(token);
    }

    @Test
    void validateTokenBadToken() {
        assertThrows(UnauthorizedException.class,
                () -> authService.validateToken("wrong2"));
    }

    @Test
    void clearCheck() throws DataAccessException {
        UserData user = new UserData("steven", "test5", null);
        DAO.createUser(user);
        String token = authService.loginData(user).authToken();
        authService.clear();
        assertNull(DAO.getAuth(token));
    }
}
