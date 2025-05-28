package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setup() throws DataAccessException {
        DAO.clear();
        userService = new UserService();
    }

    @Test
    void createUserValid() throws Exception {
        UserData newUser = new UserData("bob", "test", "test@test.com");
        UserData stored  = userService.registerUser(newUser);
        assertEquals("bob", stored.username());
        assertNotNull(DAO.getUser("bob"));
    }

    @Test
    void createUserDuplicate() throws Exception {
        userService.registerUser(new UserData("joe", "test1", null));
        assertThrows(AlreadyTakenException.class, () -> userService.registerUser(new UserData("joe", "test1", null)));
    }

    @Test
    void validateUserValid() throws Exception {
        userService.registerUser(new UserData("steve", "test2", null));
        assertDoesNotThrow(() -> userService.authorizeUser(new UserData("steve", "test2", null)));
    }

    @Test
    void validateUserInvalid() throws Exception {
        userService.registerUser(new UserData("mark", "test3", null));
        assertThrows(UnauthorizedException.class, () -> userService.authorizeUser(new UserData("mark", "wrong", null)));
    }

    @Test
    void clearCheck() throws Exception {
        userService.registerUser(new UserData("bill", "test4", null));
        userService.clear();
        assertNull(DAO.getUser("bill"));
    }
}
