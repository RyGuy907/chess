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
        return usersMap.get(username);
    }

    @Override
    public synchronized void createUser(UserData user) {
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
    @Override public AuthData getAuth(String token) {
        return tokensMap.get(token);
    }

    @Override public void deleteAuth(String token) {
        tokensMap.remove(token);
    }
}
