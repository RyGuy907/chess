package server;

import service.AuthService;
import service.UserService;
import spark.*;
import dataaccess.DAO;

public class Server {
    private UserService userService;
    private AuthService authService;
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        this.userService = new UserService();
        this.authService = new AuthService();

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.post("/user", (request, response) -> new RegistrationHandler().register(request, response, userService, authService));
        Spark.delete("/db", (request, response) -> new ClearHandler().clear(request, response));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
