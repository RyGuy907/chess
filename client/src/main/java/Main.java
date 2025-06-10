import chess.*;
import facade.ServerFacade;
import model.AuthData;
import model.GameData;
import ui.EscapeSequences;

import java.util.*;

import static ui.EscapeSequences.EMPTY;

public class Main {

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

    private static void playGame(int id, String color) throws Exception {
        GameData game = MAP.get(id);
        if (game == null) {
            printError();
            return;
        }
        ChessGame.TeamColor c;
        if (color.equalsIgnoreCase("white")) {
            c = ChessGame.TeamColor.WHITE;
            server.joinGame(game.gameID(), c, session.authToken());
            drawBoard(new ChessGame(), c);
        } else if (color.equalsIgnoreCase("black")) {
            c = ChessGame.TeamColor.BLACK;
            server.joinGame(game.gameID(), c, session.authToken());
            drawBoard(new ChessGame(), c);
        } else {
            printError();
        }
    }

    private static void observeGame(int id) throws Exception {
        GameData game = MAP.get(id);
        if (game == null) {
            printError();
            return;
        }
        drawBoard(new ChessGame(), ChessGame.TeamColor.WHITE);
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

    private static void drawBoard(ChessGame game, ChessGame.TeamColor view) {
        String light = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String dark = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String reset = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;
        Map<ChessPiece.PieceType, String> whiteMap = Map.of(ChessPiece.PieceType.KING, EscapeSequences.WHITE_KING,
                ChessPiece.PieceType.QUEEN, EscapeSequences.WHITE_QUEEN,
                ChessPiece.PieceType.BISHOP, EscapeSequences.WHITE_BISHOP,
                ChessPiece.PieceType.KNIGHT, EscapeSequences.WHITE_KNIGHT,
                ChessPiece.PieceType.ROOK, EscapeSequences.WHITE_ROOK,
                ChessPiece.PieceType.PAWN, EscapeSequences.WHITE_PAWN);
        Map<ChessPiece.PieceType, String> blackMap = Map.of(ChessPiece.PieceType.KING, EscapeSequences.BLACK_KING,
                ChessPiece.PieceType.QUEEN, EscapeSequences.BLACK_QUEEN,
                ChessPiece.PieceType.BISHOP, EscapeSequences.BLACK_BISHOP,
                ChessPiece.PieceType.KNIGHT, EscapeSequences.BLACK_KNIGHT,
                ChessPiece.PieceType.ROOK, EscapeSequences.BLACK_ROOK,
                ChessPiece.PieceType.PAWN, EscapeSequences.BLACK_PAWN);
        int rowStart;
        int rowInc;
        int colStart;
        int colInc;
        if (view == ChessGame.TeamColor.WHITE) {
            rowStart = 8;
            rowInc = -1;
            colStart = 1;
            colInc = 1;
        } else {
            rowStart = 1;
            rowInc = 1;
            colStart = 8;
            colInc = -1;
        }

        for (int r = 0; r < 8; r++) {
            int row = rowStart + r * rowInc;
            System.out.print(" " + row + " ");
            for (int c = 0; c < 8; c++) {
                int col = colStart + c * colInc;
                boolean isLight = ((row + col) & 1) == 0;
                if (isLight) {
                    System.out.print(dark);
                } else {
                    System.out.print(light);
                }

                ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    System.out.print(EMPTY);
                } else {
                    String uni;
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        uni = whiteMap.get(piece.getPieceType());
                    } else {
                        uni = blackMap.get(piece.getPieceType());
                    }
                    System.out.print(uni);
                }
                System.out.print(reset);
            }
            System.out.println();
        }
        System.out.print("   ");
        for (int c = 0; c < 8; c++) {
            char file;
            if (view == ChessGame.TeamColor.WHITE) {
                file = (char) ('a' + c);
            } else {
                file = (char) ('a' + (7 - c));
            }
            System.out.print(" " + toFullWidth(file) + " ");
        }
        System.out.println();
    }

    private static char toFullWidth(char ch) {
        if (ch >= 0x21 && ch <= 0x7E) {
            return (char) (ch - 0x20 + 0xFF00);
        }
        return ch;
    }
}
