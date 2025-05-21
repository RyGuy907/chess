package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;

public class DAO {

    private static final DAOInstance INSTANCE = new TemporaryDB();

    public static UserData getUser(String username) {
        return INSTANCE.getUser(username);
    }

    public static void createUser(UserData user) {
        INSTANCE.createUser(user);
    }

    public static AuthData createAuth(String username) {
        return INSTANCE.createAuth(username);
    }

    public static void clear() {
        INSTANCE.clear();
    }

    public static AuthData getAuth(String token) {
        return INSTANCE.getAuth(token);
    }

    public static void deleteAuth(String token) {
        INSTANCE.deleteAuth(token);
    }

    public static int createGame(GameData g) {
        return INSTANCE.createGame(g);
    }

    public static GameData getGame(int id) {
        return INSTANCE.getGame(id);
    }

    public static GameData[] listGames() {
        return INSTANCE.listGames();
    }
    public static void updateGame(GameData g) {
        INSTANCE.updateGame(g); }
    }
