package server;

import dataaccess.UnauthorizedException;
import service.AuthService;
import spark.Request;
import spark.Response;

public class LogoutHandler {

    public Object logout(Request request, Response response, AuthService authService) {
        response.type("application/json");
        ErrorHandler errors = new ErrorHandler();
        String token = request.headers("authorization");
        try {
            authService.logout(token);
            response.status(200);
            return "{}";
        } catch (UnauthorizedException exception) {
            return errors.handleError(exception, response, 401);
        } catch (Exception exception) {
            return errors.handleError(exception, response, 500);
        }
    }
}