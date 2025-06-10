package service;

import dataaccess.UnauthorizedException;
import dataaccess.DAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class AuthService {

    public AuthData loginData(UserData user) throws DataAccessException {
        return DAO.createAuth(user.username());
    }
    public void logoutData(String authToken) throws UnauthorizedException, DataAccessException {
        if (DAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        DAO.deleteAuth(authToken);
    }
    public String getUsername(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = DAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException ("Unauthorized");
        } else {
            return auth.username();
        }
    }

    public void validateToken(String authToken) throws UnauthorizedException, DataAccessException {
        if (DAO.getAuth(authToken) == null) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
    public void clear() throws DataAccessException {
        DAO.clear();
    }

}
