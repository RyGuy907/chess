package service;

import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import dataaccess.DAO;

public class AuthService {
    public AuthData loginUser(UserData user) throws UnauthorizedException, DataAccessException {
        if (user == null || user.username() == null || user.username().isBlank() || user.password() == null || user.password().isBlank()) {
            throw new UnauthorizedException("Unauthorized");
        }
        UserData authdata = DAO.getUser(user.username());
        if (authdata == null || !authdata.password().equals(user.password())) {
            throw new UnauthorizedException("Unauthorized");
        }
        return DAO.createAuth(user.username());
    }
}
