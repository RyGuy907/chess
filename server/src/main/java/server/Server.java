package server;

import spark.Spark;
import service.*;

public class Server {
    private final UserService userService = new UserService();
    private final AuthService authService = new AuthService();
    private final GameService gameService = new GameService();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.post("/user", (request, response) -> new RegistrationHandler().register(request, response, userService, authService));
        Spark.post("/session", (request, response) -> new LoginHandler().login(request, response, userService, authService));
        Spark.delete("/session", (request, response) -> new LogoutHandler().logout(request, response, authService));
        Spark.delete("/db", (request, response) -> new ClearHandler().clear(response));
        Spark.get("/game", (request, response) -> new ListGamesHandler().listGames(request, response, gameService, authService));
        Spark.post("/game", (request, response) -> new CreateGameHandler().createGame(request, response, gameService, authService));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
