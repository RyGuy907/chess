package server;

import spark.Request;
import spark.Response;
import dataaccess.DAO;

public class ClearHandler {
    public Object clear(Response response) {
        DAO.clear();
        response.status(200);
        response.type("application/json");
        return "{}";
    }
}