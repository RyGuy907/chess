package service;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DAO;
import dataaccess.UnauthorizedException;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.Objects;

public class GameService {
    private final AuthService auth = new AuthService();
    public GameData[] listGames(String authToken) throws UnauthorizedException, DataAccessException {
        auth.validateToken(authToken);
        return DAO.listGames();
    }
    public int createGame(String authToken, String gameName) throws BadRequestException, UnauthorizedException, DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("Bad Request");
        }
        AuthService auth = new AuthService();
        auth.validateToken(authToken);
        GameData game = new GameData(0, null, null, gameName, null);
        return DAO.createGame(game);
    }

    public GameData getGame(int gameID) throws DataAccessException  {
        return DAO.getGame(gameID);
    }

    public void updateGame(GameData game) throws DataAccessException {
        DAO.updateGame(game);
    }

    public void leaveGame(String username, int gameID) throws DataAccessException {
        GameData g = DAO.getGame(gameID);
        if (g == null) return;
        if (Objects.equals(g.whiteUsername(), username)) {
            g = new GameData(g.gameID(), null, g.blackUsername(), g.gameName(), g.game());
        } else if (Objects.equals(g.blackUsername(), username)) {
            g = new GameData(g.gameID(), g.whiteUsername(), null, g.gameName(), g.game());
        }
        DAO.updateGame(g);
    }
    public void joinGame(String token, String color, Integer gameID) throws BadRequestException, UnauthorizedException,
            AlreadyTakenException, DataAccessException {
        if (color == null || gameID == null) {
            throw new BadRequestException("Bad Request");
        }
        if (!("WHITE".equals(color) || "BLACK".equals(color))) {
            throw new BadRequestException("Bad Request");
        }
        AuthData auth = DAO.getAuth(token);
        if (auth == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        String username = auth.username();
        GameData currentGame = DAO.getGame(gameID);
        if (currentGame == null) {
            throw new BadRequestException("Bad Request");
        }
        if ("WHITE".equals(color)) {
            if (currentGame.whiteUsername() != null && !currentGame.whiteUsername().equals(username)) {
                throw new AlreadyTakenException("Already Taken");
            }
            currentGame = new GameData(currentGame.gameID(), username, currentGame.blackUsername(),
                    currentGame.gameName(), currentGame.game());
        } else {
            if (currentGame.blackUsername() != null && !currentGame.blackUsername().equals(username)) {
                throw new AlreadyTakenException("Already Taken");
            }
            currentGame = new GameData(currentGame.gameID(), currentGame.whiteUsername(), username, currentGame.gameName(), currentGame.game());
        }
        DAO.updateGame(currentGame);
    }
    public void clear() throws DataAccessException {
        DAO.clear();
    }
}
