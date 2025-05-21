package service;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import model.UserData;
import dataaccess.DAO;
import dataaccess.DataAccessException;

public class UserService {
    public UserData registerUser(UserData user) throws Exception {
        UserData username = DAO.getUser(user.username());
        if (username != null) {
            throw new AlreadyTakenException("Username Already Taken");
        }
        else if ((user.password() == null) || (user.password().isEmpty())) {
            throw new BadRequestException("Bad Request");
        }
        else {
            DAO.createUser(user);
            return DAO.getUser(user.username());
        }
    }
}
