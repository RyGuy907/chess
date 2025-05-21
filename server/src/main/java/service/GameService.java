package service;

import dataaccess.DAO;
import dataaccess.UnauthorizedException;
import model.GameData;

public class GameService {
    private final AuthService auth = new AuthService();
    public GameData[] listGames(String authToken) throws UnauthorizedException {
        auth.validateToken(authToken);
        return DAO.listGames();
    }
}
