import chess.*;
import facade.ServerFacade;
import model.AuthData;
import model.GameData;
import ui.EscapeSequences;
import ui.InGame;

import java.util.*;

public class main2 {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static ServerFacade server;
    private static AuthData session;
    private static final Map<Integer, GameData> MAP = new HashMap<>();

    public static void main(String[] args) throws Exception {
        int port = 8080;
        server = new ServerFacade(port);
        System.out.println(EscapeSequences.SET_TEXT_BOLD +
                "Welcome to 240 Chess Client ♕. Type 'help' to get started."
                + EscapeSequences.RESET_TEXT_BOLD_FAINT);
        while (true) {
            if (session == null) {
                System.out.print("[Status:Logged_Out] >>> ");
            } else {
                System.out.print("[Status:Logged_In] >>> ");
            }

            String line = SCANNER.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            try {
                if (session == null) {
                    handleBefore(parts);
                } else {
                    handleAfter(parts);
                }
            } catch (Exception exception) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                        "Error: Invalid, nonexistent, or taken credentials (type 'help')"
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private static void handleBefore(String[] args) throws Exception {
        switch (args[0].toLowerCase()) {
            case "help" -> {
                loggedOutHelp();
            }
            case "quit" -> {
                System.exit(0);
            }
            case "login" -> {
                if (args.length == 3) {
                    login(args[1], args[2]);
                } else {
                    printError();
                }
            }
            case "register" -> {
                if (args.length == 4) {
                    register(args[1], args[2], args[3]);
                } else {
                    printError();
                }
            }
            default -> {
                printError();
            }
        }
    }

    private static void handleAfter(String[] args) throws Exception {
        switch (args[0].toLowerCase()) {
            case "help" -> {
                loggedInHelp();
            }
            case "logout" -> {
                if (args.length == 1) {
                    logout();
                } else {
                    printError();
                }
            }
            case "create" -> {
                if (args.length >= 2) {
                    createGame(joinRest(args, 1));
                } else {
                    printError();
                }
            }
            case "list" -> {
                if (args.length == 1) {
                    listGames();
                } else {
                    printError();
                }
            }
            case "play" -> {
                if (args.length == 3) {
                    playGame(Integer.parseInt(args[1]), args[2]);
                } else {
                    printError();
                }
            }
            case "observe" -> {
                if (args.length == 2) {
                    observeGame(Integer.parseInt(args[1]));
                } else {
                    printError();
                }
            }
            default -> {
                printError();
            }
        }
    }

    private static void register(String username, String password, String email) throws Exception {
        session = server.register(username, password, email);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully registered" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void login(String username, String password) throws Exception {
        session = server.login(username, password);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully logged in" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void logout() throws Exception {
        server.logout(session.authToken());
        session = null;
        MAP.clear();
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully logged out" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void createGame(String name) throws Exception {
        server.createGame(name, session.authToken());
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully created" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void listGames() throws Exception {
        GameData[] g = server.listGames(session.authToken());
        MAP.clear();
        for (int i = 0; i < g.length; i++) {
            MAP.put(i + 1, g[i]);
            System.out.printf(EscapeSequences.SET_TEXT_COLOR_YELLOW + "%d. %s  [white=%s  black=%s]%n", i + 1, g[i].gameName(),
                    Objects.toString(g[i].whiteUsername(), " "),
                    Objects.toString(g[i].blackUsername(), " ") + EscapeSequences.RESET_TEXT_COLOR);
        }
        if (g.length == 0) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "No active games" + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private static void playGame(int index, String colorString) {
        try {
            GameData game = MAP.get(index);
            if (game == null) { printError(); return; }
            ChessGame.TeamColor color;
            if (colorString.equalsIgnoreCase("white")) {
                color = ChessGame.TeamColor.WHITE;
            } else if (colorString.equalsIgnoreCase("black")) {
                color = ChessGame.TeamColor.BLACK;
            } else {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                        "Error: Color must be white or black" +
                        EscapeSequences.RESET_TEXT_COLOR);
                return;
            }
            server.joinGame(game.gameID(), color, session.authToken());
            String user = session.username();
            new InGame(game.game(), color, user, session.authToken(),
                    game.gameID())
                    .beginSession();
        } catch (Exception ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                    "Error: Failed to join game (" + ex.getMessage() + ")" +
                    EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private static void observeGame(int index) {
        try {
            GameData game = MAP.get(index);
            if (game == null) { printError(); return; }
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN +
                    "Successfully observing" +
                    EscapeSequences.RESET_TEXT_COLOR);
            String user = session.username();
            new InGame(game.game(), null, user,
                    session.authToken(), game.gameID())
                    .beginSession();

        } catch (Exception ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                    "Error: Failed to observe game (" + ex.getMessage() + ")" +
                    EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private static void loggedOutHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + """
                register <username> <password> <email>
                login    <username> <password>
                help
                quit""" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void loggedInHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + """
                create  <game‑name>
                list
                play    <game-index> <white|black>
                observe <game-index>
                logout
                help""" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void printError() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid syntax (type 'help')" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static String joinRest(String[] a, int start) {
        return String.join(" ", Arrays.copyOfRange(a, start, a.length));
    }

}