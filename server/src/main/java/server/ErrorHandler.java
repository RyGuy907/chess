package server;

import com.google.gson.Gson;

import java.util.Map;
import spark.Response;

public class ErrorHandler {
    public Object handleError(Exception exception, Response response, int statusCode) {
        var serializer = new Gson();
        String body = serializer.toJson(Map.of("message", "Error: " + exception.getMessage(), "success", false));
        response.type("application/json");
        response.status(statusCode);
        return body;
    }
}
