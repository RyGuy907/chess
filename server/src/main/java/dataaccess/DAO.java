package dataaccess;

import model.UserData;
import model.AuthData;

public class DAO {

    private static final DAOInstance instance = new TemporaryDB();

    public static UserData getUser(String username) {
        return instance.getUser(username);
    }

    public static void createUser(UserData user) {
        instance.createUser(user);
    }

    public static AuthData createAuth(String username) {
        return instance.createAuth(username);
    }

    public static void clear() {
        instance.clear();
    }
}
