package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.*;

public class DAOFunctionsSQL implements DAOInstance {
    private static final Gson SERIALIZER = new Gson();

    public DAOFunctionsSQL() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS Auth");
            statement.executeUpdate("DROP TABLE IF EXISTS Game");
            statement.executeUpdate("DROP TABLE IF EXISTS User");
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS User (
                  username VARCHAR(50) PRIMARY KEY,
                  password CHAR(60) NOT NULL,
                  email VARCHAR(256)
                )
            """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Auth (
                  authToken CHAR(36) PRIMARY KEY,
                  username VARCHAR(50) NOT NULL,
                  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Game (
                  gameID INT AUTO_INCREMENT PRIMARY KEY,
                  whiteUsername VARCHAR(50),
                  blackUsername VARCHAR(50),
                  gameName VARCHAR(256) NOT NULL,
                  chessGame JSON NOT NULL
                )
            """);
        } catch (SQLException exception) {
            throw new DataAccessException("schema failed", exception);
        }
    }

    private int update(String sql, Object... p) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, p);
            ps.executeUpdate();
            return firstGeneratedKey(ps);
        } catch (SQLException exception) {
            throw new DataAccessException("DB update failed: " + exception.getMessage(), exception);
        }
    }

    private void bindParams(PreparedStatement ps, Object[] pam) throws SQLException {
        for (int i = 0; i < pam.length; i++) {
            final int idx = i + 1;
            Object p = pam[i];
            if (p == null) {
                ps.setNull(idx, Types.NULL); continue;
            }
            if (p instanceof String s) {
                ps.setString(idx, s);
            } else if (p instanceof Integer v) {
                ps.setInt(idx, v);
            } else if (p instanceof Long v) {
                ps.setLong(idx, v);
            } else if (p instanceof Double v) {
                ps.setDouble(idx, v);
            } else if (p instanceof Boolean v) {
                ps.setBoolean(idx, v);
            } else {
                ps.setObject(idx, p);
            }
        }
    }

    private int firstGeneratedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var hash = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        update("INSERT INTO User (username,password,email) VALUES (?,?,?)",
                user.username(), hash, user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT * FROM User WHERE username=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")); }
            }
            return null;
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage(), exception);
        }
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        update("INSERT INTO Auth (authToken,username) VALUES (?,?)", token, username);
        return new AuthData(token, username);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        var sql = "SELECT username FROM Auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? new AuthData(token, rs.getString("username")) : null;
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        update("DELETE FROM Auth WHERE authToken=?", token);
    }

    @Override
    public int createGame(GameData template) throws DataAccessException {
        ChessGame game = template.game() != null ? template.game() : new ChessGame();
        int id = update("""
                INSERT INTO Game (whiteUsername,blackUsername,gameName,chessGame)
                VALUES (?,?,?,?)""",
                template.whiteUsername(),
                template.blackUsername(),
                template.gameName(),
                SERIALIZER.toJson(game));
        return id;
    }

    @Override
    public GameData getGame(int id) throws DataAccessException {
        var sql = "SELECT * FROM Game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChessGame game = SERIALIZER.fromJson(rs.getString("chessGame"), ChessGame.class);
                    return new GameData(
                            id, rs.getString("whiteUsername"), rs.getString("blackUsername"),
                            rs.getString("gameName"), game);
                }
            }
            return null;
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage(), exception);
        }
    }

    @Override
    public GameData[] listGames() throws DataAccessException {
        var sql = "SELECT gameID,whiteUsername,blackUsername,gameName FROM Game";
        List<GameData> list = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new GameData(
                        rs.getInt("gameID"), rs.getString("whiteUsername"),
                        rs.getString("blackUsername"), rs.getString("gameName"),
                        null));
            }
        } catch (SQLException exception) {
            throw new DataAccessException(exception.getMessage(), exception);
        }
        return list.toArray(new GameData[0]);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        update("""
                UPDATE Game
                SET whiteUsername=?, blackUsername=?, gameName=?, chessGame=?
                WHERE gameID=?""",
                game.whiteUsername(), game.blackUsername(), game.gameName(),
                SERIALIZER.toJson(game.game()), game.gameID());
    }

    @Override
    public void clear() throws DataAccessException {
        update("DELETE FROM Auth");
        update("DELETE FROM Game");
        update("DELETE FROM User");
    }
}
