package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;

public interface DAOInstance {
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    AuthData createAuth(String username) throws DataAccessException;
    void clear() throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;
    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    GameData[] listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}
