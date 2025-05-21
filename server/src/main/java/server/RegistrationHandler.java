package server;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import model.AuthData;
import model.UserData;
import service.AuthService;
import service.UserService;
import spark.Response;
import spark.Request;
import server.ErrorHandler;

public class RegistrationHandler {
    public Object register(Request request, Response response, UserService userService, AuthService authService) {
        response.type("application/json");
        ErrorHandler errorHandler = new ErrorHandler();
        try {
            var serializer = new Gson();
            UserData user = userService.registerUser(serializer.fromJson(request.body(), UserData.class));
            AuthData auth = authService.loginUser(user);
            response.status(200);
            return serializer.toJson(auth);
        }
        catch (BadRequestException error) {
            return errorHandler.handleError(error, response, 400);
        }
        catch (AlreadyTakenException error) {
            return errorHandler.handleError(error, response, 403);
        }
        catch (Exception error) {
            return errorHandler.handleError(error, response, 500);
        }
    }
}
