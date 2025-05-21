package dataaccess;

import model.GameData;
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

    public static AuthData getAuth(String token) {
        return instance.getAuth(token);
    }

    public static void deleteAuth(String token) {
        instance.deleteAuth(token);
    }

    public static int createGame(GameData g) {
        return instance.createGame(g);
    }

    public static GameData getGame(int id) {
        return instance.getGame(id);
    }

    public static GameData[] listGames() {
        return instance.listGames();
    }
    public static void updateGame(GameData g) {
        instance.updateGame(g); }
    }
