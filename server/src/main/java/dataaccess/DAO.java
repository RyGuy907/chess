package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;

public class DAO {

    private static final DAOInstance INSTANCE = new DAOFunctionsSQL();

    public static UserData getUser(String username) throws DataAccessException  {
        return INSTANCE.getUser(username);
    }

    public static void createUser(UserData user) throws DataAccessException {
        INSTANCE.createUser(user);
    }

    public static AuthData createAuth(String username) throws DataAccessException {
        return INSTANCE.createAuth(username);
    }

    public static void clear() throws DataAccessException {
        INSTANCE.clear();
    }

    public static AuthData getAuth(String token) throws DataAccessException {
        return INSTANCE.getAuth(token);
    }

    public static void deleteAuth(String token) throws DataAccessException {
        INSTANCE.deleteAuth(token);
    }

    public static int createGame(GameData g) throws DataAccessException {
        return INSTANCE.createGame(g);
    }

    public static GameData getGame(int id) throws DataAccessException {
        return INSTANCE.getGame(id);
    }

    public static GameData[] listGames() throws DataAccessException {
        return INSTANCE.listGames();
    }
    public static void updateGame(GameData g) throws DataAccessException {
        INSTANCE.updateGame(g); }
    }
