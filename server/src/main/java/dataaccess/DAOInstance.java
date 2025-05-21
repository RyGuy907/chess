package dataaccess;

import model.GameData;
import model.UserData;
import model.AuthData;

public interface DAOInstance {
    UserData getUser(String username);
    void createUser(UserData user);
    AuthData createAuth(String username);
    void clear();
    AuthData getAuth(String token);
    void deleteAuth(String token);
    int createGame(GameData game);
    GameData getGame(int gameID);
    GameData[] listGames();
}
