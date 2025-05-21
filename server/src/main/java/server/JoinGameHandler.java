package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Map;

public class JoinGameHandler {

    public Object joinGame(Request request, Response response, GameService gameService, AuthService authService) {
        response.type("application/json");
        ErrorHandler errors = new ErrorHandler();
        try {
            var serializer = new Gson();
            Map<?, ?> map = serializer.fromJson(request.body(), Map.class);
            String color = null;
            if (map != null) {
                color = (String) map.get("playerColor");
            }
            Integer gameID = null;
            if (map != null && map.get("gameID") != null) {
                gameID = ((Number) map.get("gameID")).intValue();
            }
            String token = request.headers("authorization");
            gameService.joinGame(token, color, gameID);
            response.status(200);
            return "{}";
        } catch (BadRequestException exception) {
            return errors.handleError(exception, response, 400);

        } catch (UnauthorizedException exception) {
            return errors.handleError(exception, response, 401);

        } catch (AlreadyTakenException exception) {
            return errors.handleError(exception, response, 403);

        } catch (Exception exception) {
            return errors.handleError(exception, response, 500);
        }
    }
}
