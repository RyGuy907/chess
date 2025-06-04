package facade;

import chess.ChessGame;
import com.google.gson.*;
import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ServerFacade {

    private final String address;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson serializer = new Gson();

    public ServerFacade(int port) {
        address = "http://" + "localhost" + ":" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        return send("POST", "/user", body, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var body = Map.of("username", username, "password", password);
        return send("POST", "/session", body, null, AuthData.class);
    }

    public void logout(String token) throws Exception {
        send("DELETE", "/session", null, token, Void.class);
    }

    public GameData createGame(String name, String token) throws Exception {
        var body = Map.of("gameName", name);
        return send("POST", "/game", body, token, GameData.class);
    }

    public GameData joinGame(int id, ChessGame.TeamColor color, String token) throws Exception {
        var body = Map.of("playerColor", color, "gameID", id);
        return send("PUT", "/game", body, token, GameData.class);
    }

    public GameData[] listGames(String token) throws Exception {
        JsonObject object = send("GET", "/game", null, token, JsonObject.class);
        return serializer.fromJson(object.get("games"), GameData[].class);
    }

    public void clear() throws Exception {
        send("DELETE", "/db", null, null, Void.class);
    }

    private <T> T send(String method, String path, Object body, String token, Class<T> type) throws Exception {
        HttpRequest request = build(method, path, body, token);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return handle(response, type);
    }

    private HttpRequest build(String method, String path, Object body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(address + path))
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (token != null) builder.header("Authorization", token);
        if (body != null) {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(serializer.toJson(body),
                            StandardCharsets.UTF_8));
        }
        return builder.build();
    }

    private <T> T handle(HttpResponse<String> response, Class<T> type) {
        if (response.statusCode() == 200) {
            return type == Void.class ? null : serializer.fromJson(response.body(), type);
        }
        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
    }
}
