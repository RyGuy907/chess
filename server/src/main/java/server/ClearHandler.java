package server;

import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import dataaccess.DAO;

public class ClearHandler {
    public Object clear(Response response) {
        try {
            DAO.clear();
            response.status(200);
            response.type("application/json");
            return "{}";
        } catch (DataAccessException ex) {
            response.status(500);
            response.type("application/json");
            return "{\"message\":\"Error: " + ex.getMessage() + "\"}";
        }
    }
}