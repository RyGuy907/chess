package service;

import dataaccess.UnauthorizedException;
import dataaccess.DAO;
import model.AuthData;
import model.UserData;

public class AuthService {

    public AuthData loginData(UserData user) {
        return DAO.createAuth(user.username());
    }
    public void logoutData(String authToken) throws UnauthorizedException {
        if (DAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        DAO.deleteAuth(authToken);
    }
    public void validateToken(String authToken) throws UnauthorizedException {
        if (DAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

}
