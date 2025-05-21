package server;

import com.google.gson.Gson;
import dataaccess.UnauthorizedException;
import model.GameData;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class ListGamesHandler {

    public Object listGames(Request request, Response response, GameService gameService, AuthService authService) {
        response.type("application/json");
        ErrorHandler errors = new ErrorHandler();
        try {
            var serializer = new Gson();
            String token = request.headers("authorization");
            GameData[] games = gameService.listGames(token);
            Map<String, Object> data = new HashMap<>();
            data.put("games", games);
            response.status(200);
            return serializer.toJson(data);
        } catch (UnauthorizedException exception) {
            return errors.handleError(exception, response, 401);

        } catch (Exception exception) {
            return errors.handleError(exception, response, 500);
        }
    }
}
