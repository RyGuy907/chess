package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporaryDB implements DAOInstance {

    private final Map<String, UserData> usersMap = new HashMap<>();
    private final Map<String, AuthData> tokensMap = new HashMap<>();
    private final Map<Integer, GameData> gamesMap = new HashMap<>();

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
    private int i = 1;
    @Override
    public synchronized int createGame(GameData game) {
        int id = i++;
        gamesMap.put(id, new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
        return id;
    }
    @Override
    public synchronized GameData getGame(int id) {
        return gamesMap.get(id);
    }

    @Override
    public synchronized GameData[] listGames() {
        return gamesMap.values().toArray(new GameData[0]);
    }

    @Override public synchronized void clear() {
        usersMap.clear();
        tokensMap.clear();
        gamesMap.clear();
        i = 1;
    }
    @Override public AuthData getAuth(String token) {
        return tokensMap.get(token);
    }

    @Override public void deleteAuth(String token) {
        tokensMap.remove(token);
    }
}
