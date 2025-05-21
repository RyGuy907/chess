package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class CreateGameHandler {

    public Object createGame(Request request, Response response, GameService gameService, AuthService authService) {
        response.type("application/json");
        ErrorHandler errors = new ErrorHandler();
        try {
            String authToken = request.headers("authorization");
            var serializer = new Gson();
            Map<?, ?> body = serializer.fromJson(request.body(), Map.class);
            String gameName = body == null ? null : (String) body.get("gameName");
            int id = gameService.createGame(authToken, gameName);
            Map<String, Object> data = new HashMap<>();
            data.put("gameID", id);
            response.status(200);
            return serializer.toJson(data);
        } catch (BadRequestException exception) {
            return errors.handleError(exception, response, 400);

        } catch (UnauthorizedException exception) {
            return errors.handleError(exception, response, 401);

        } catch (Exception exception) {
            return errors.handleError(exception, response, 500);
        }
    }
}
