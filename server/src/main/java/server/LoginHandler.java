package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import service.AuthService;
import service.UserService;
import spark.Request;
import spark.Response;

public class LoginHandler {

    public Object login(Request request, Response response, UserService userService, AuthService authService) {
        response.type("application/json");
        ErrorHandler errors = new ErrorHandler();
        try {
            var serializer = new Gson();
            UserData user = serializer.fromJson(request.body(), UserData.class);
            userService.authorizeUser(user);
            AuthData auth = authService.loginData(user);
            response.status(200);
            return serializer.toJson(auth);

        } catch (BadRequestException exception) {
            return errors.handleError(exception, response, 400);

        } catch (UnauthorizedException exception) {
            return errors.handleError(exception, response, 401);

        } catch (Exception exception) {
            return errors.handleError(exception, response, 500);
        }
    }
}
