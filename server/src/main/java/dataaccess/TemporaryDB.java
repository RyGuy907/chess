package dataaccess;

import model.UserData;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporaryDB implements DAOInstance {

    private final Map<String, UserData> usersMap = new HashMap<>();
    private final Map<String, AuthData> tokensMap = new HashMap<>();

    @Override
    public synchronized UserData getUser(String username) {
        return usersMap.get(username);                 // returns null if missing
    }

    @Override
    public synchronized void createUser(UserData user) {
        // blindly insert; duplicate checks live in the service layer
        usersMap.put(user.username(), user);
    }

    @Override
    public synchronized AuthData createAuth(String username) {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        tokensMap.put(token, auth);
        return auth;
    }

    @Override
    public synchronized void clear() {
        usersMap.clear();
        tokensMap.clear();
    }
}
