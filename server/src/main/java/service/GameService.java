package service;

import dataaccess.BadRequestException;
import dataaccess.DAO;
import dataaccess.UnauthorizedException;
import model.GameData;

public class GameService {
    private final AuthService auth = new AuthService();
    public GameData[] listGames(String authToken) throws UnauthorizedException {
        auth.validateToken(authToken);
        return DAO.listGames();
    }
    public int createGame(String authToken, String gameName) throws BadRequestException, UnauthorizedException {
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("Bad Request");
        }
        AuthService auth = new AuthService();
        auth.validateToken(authToken);
        GameData game = new GameData(0, null, null, gameName, null);
        return DAO.createGame(game);
    }
}
