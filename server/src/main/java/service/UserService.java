package service;

import dataaccess.*;
import model.UserData;
import dataaccess.DataAccessException;

public class UserService {

    public UserData registerUser(UserData userdata) throws AlreadyTakenException, BadRequestException, DataAccessException {
        if (userdata == null || userdata.username() == null ||
                userdata.username().isBlank() || userdata.password() == null ||
                userdata.password().isBlank()) {
            throw new BadRequestException("Bad Request");
        }
        if (DAO.getUser(userdata.username()) != null) {
            throw new AlreadyTakenException("Already Taken");
        }

        DAO.createUser(userdata);
        return DAO.getUser(userdata.username());
    }
    public void clear() throws DataAccessException {
        DAO.clear();
    }
    public void authorizeUser(UserData userdata) throws BadRequestException, UnauthorizedException, DataAccessException {
        if (userdata == null || userdata.username() == null ||
                userdata.username().isBlank() || userdata.password() == null ||
                userdata.password().isBlank()) {
            throw new BadRequestException("Bad Request");
        }
        UserData stored = DAO.getUser(userdata.username());
        if (stored == null || !stored.password().equals(userdata.password())) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
