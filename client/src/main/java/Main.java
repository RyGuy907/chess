import chess.*;
import facade.ServerFacade;
import facade.WebSocketFacade;
import model.AuthData;
import model.GameData;
import ui.EscapeSequences;
import ui.MessageHandler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ui.EscapeSequences.EMPTY;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static ServerFacade server;
    private static AuthData session;
    private static ChessGame currentBoard;
    private static WebSocketFacade websocket;
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

    private static void handleInGame(ChessGame.TeamColor view,
                                     boolean isPlayer) throws Exception {

        boolean running = true;
        System.out.println("Type 'help' for in‑game commands.");

        while (running) {
            System.out.print("[game] >>> ");
            String[] args = SCANNER.nextLine().trim().split("\\s+");
            if (args.length == 0 || args[0].isEmpty()) continue;

            switch (args[0].toLowerCase()) {
                case "help" -> inGameHelp();

                case "move" -> {
                    if (!isPlayer) { printError(); break; }
                    if (args.length == 3) {
                        websocket.makeMove(parseMove(args[1], args[2]));
                        // board will refresh automatically when LOAD_GAME arrives
                    } else printError();
                }

                case "highlight" -> {
                    if (args.length == 2) {
                        Set<ChessPosition> hilite = highlight(args[1]);   // returns the set
                        drawBoard(currentBoard, view, hilite);
                    } else printError();
                }

                case "redraw" -> {
                    if (args.length == 1) {
                        drawBoard(currentBoard, view, null);
                    } else printError();
                }

                case "resign" -> {
                    if (!isPlayer) { printError(); break; }
                    if (args.length == 1) websocket.resign();
                    else printError();
                }

                case "leave" -> {
                    if (args.length == 1) {
                        websocket.leaveGame();
                        websocket.close();
                        running = false;
                    } else printError();
                }

                default -> printError();
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
        if (websocket != null) { websocket.close(); websocket = null; }
        server.logout(session.authToken());
        session = null; MAP.clear();
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
            websocket = new WebSocketFacade(
                    "http://localhost:8080",
                    uiHandler(c),
                    session.authToken(),
                    game.gameID());
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined game" + EscapeSequences.RESET_TEXT_COLOR);
            handleInGame(c, true);
        } else if (color.equalsIgnoreCase("black")) {
            c = ChessGame.TeamColor.BLACK;
            server.joinGame(game.gameID(), c, session.authToken());
            websocket = new WebSocketFacade("http://localhost:8080",
                    uiHandler(c), session.authToken(), game.gameID());
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined game" + EscapeSequences.RESET_TEXT_COLOR);
            handleInGame(c, true);
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
        websocket = new WebSocketFacade("http://localhost:8080",
                uiHandler(ChessGame.TeamColor.WHITE), session.authToken(), game.gameID());
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully observing game" + EscapeSequences.RESET_TEXT_COLOR);
        handleInGame(ChessGame.TeamColor.WHITE, false);

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

    private static void inGameHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + """
                        move      <from> <to>  (players only)
                        highlight <square>
                        redraw
                        resign                 (players only)
                        leave""" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static void printError() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid syntax (type 'help')" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private static String joinRest(String[] a, int start) {
        return String.join(" ", Arrays.copyOfRange(a, start, a.length));
    }

    private static void drawBoard(ChessGame game,
                                  ChessGame.TeamColor view,
                                  Set<ChessPosition> highlights) {

        if (game == null) {
            System.out.println("Board not ready.");
            return;
        }

        final String LIGHT  = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        final String DARK   = EscapeSequences.SET_BG_COLOR_DARK_GREY;
        final String HILITE = EscapeSequences.SET_BG_COLOR_GREEN;          // ❷ new
        final String RESET  = EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR;

        Map<ChessPiece.PieceType,String> whiteMap = Map.of(
                ChessPiece.PieceType.KING,   EscapeSequences.WHITE_KING,
                ChessPiece.PieceType.QUEEN,  EscapeSequences.WHITE_QUEEN,
                ChessPiece.PieceType.BISHOP, EscapeSequences.WHITE_BISHOP,
                ChessPiece.PieceType.KNIGHT, EscapeSequences.WHITE_KNIGHT,
                ChessPiece.PieceType.ROOK,   EscapeSequences.WHITE_ROOK,
                ChessPiece.PieceType.PAWN,   EscapeSequences.WHITE_PAWN);

        Map<ChessPiece.PieceType,String> blackMap = Map.of(
                ChessPiece.PieceType.KING,   EscapeSequences.BLACK_KING,
                ChessPiece.PieceType.QUEEN,  EscapeSequences.BLACK_QUEEN,
                ChessPiece.PieceType.BISHOP, EscapeSequences.BLACK_BISHOP,
                ChessPiece.PieceType.KNIGHT, EscapeSequences.BLACK_KNIGHT,
                ChessPiece.PieceType.ROOK,   EscapeSequences.BLACK_ROOK,
                ChessPiece.PieceType.PAWN,   EscapeSequences.BLACK_PAWN);

        int rowStart = (view == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int rowInc   = (view == ChessGame.TeamColor.WHITE) ? -1 : 1;
        int colStart = (view == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int colInc   = (view == ChessGame.TeamColor.WHITE) ? 1 : -1;

        for (int r = 0; r < 8; r++) {
            int row = rowStart + r * rowInc;
            System.out.print(" " + row + " ");

            for (int c = 0; c < 8; c++) {
                int col = colStart + c * colInc;
                ChessPosition pos = new ChessPosition(row, col);

                boolean isHighlight = highlights != null && highlights.contains(pos);
                boolean isLightSq   = ((row + col) & 1) == 0;

                if (isHighlight)      System.out.print(HILITE);
                else if (isLightSq)   System.out.print(DARK);
                else                  System.out.print(LIGHT);

                ChessPiece piece = game.getBoard().getPiece(pos);
                if (piece == null) System.out.print(EMPTY);
                else System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? whiteMap.get(piece.getPieceType())
                        : blackMap.get(piece.getPieceType()));

                System.out.print(RESET);
            }
            System.out.println();
        }
        System.out.print("   ");
        for (int c = 0; c < 8; c++) {
            char file = (view == ChessGame.TeamColor.WHITE) ? (char) ('a' + c)
                    : (char) ('a' + (7 - c));
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

    private static volatile boolean gameOver = false;

    private static MessageHandler uiHandler(ChessGame.TeamColor view) {
        return message -> {
            switch (message.getServerMessageType()) {

                case LOAD_GAME:
                    websocket.messages.GameMessage gm =
                            (websocket.messages.GameMessage) message;
                    GameData gd = (GameData) gm.game;
                    currentBoard = gd.game();
                    drawBoard(currentBoard, view, null);
                    gameOver = false;
                    break;

                case NOTIFICATION:
                    String note = ((websocket.messages.NotificationMessage) message).message;
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW +
                            note +
                            EscapeSequences.RESET_TEXT_COLOR);

                    String low = note.toLowerCase();
                    if (low.contains("checkmate") || low.contains("resigned") || low.contains("stalemate")) {
                        gameOver = true;
                    }
                    break;

                case ERROR:
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                            ((websocket.messages.ErrorMessage) message).errorMessage +
                            EscapeSequences.RESET_TEXT_COLOR);
                    break;
            }
        };
    }

    private static ChessMove parseMove(String from, String to) {
        ChessPosition s = new ChessPosition(from.charAt(1)-'0', from.charAt(0)-'a'+1);
        ChessPosition e = new ChessPosition(to.charAt(1)-'0', to.charAt(0)-'a'+1);
        return new ChessMove(s, e, null);
    }

    private static Set<ChessPosition> highlight(String square) {
        if (currentBoard == null) {
            System.out.println("Board not ready.");
            return Set.of();
        }

        if (square.length() != 2 ||
                square.charAt(0) < 'a' || square.charAt(0) > 'h' ||
                square.charAt(1) < '1' || square.charAt(1) > '8') {
            System.out.println("Bad square (use a‑h and 1‑8).");
            return Set.of();
        }

        int col = square.charAt(0) - 'a' + 1;   // a‑>1, b‑>2 …
        int row = square.charAt(1) - '0';       // '1'‑>'1'
        ChessPosition origin = new ChessPosition(row, col);
        ChessPiece piece = currentBoard.getBoard().getPiece(origin);
        if (piece == null) {
            System.out.println("No piece on " + square + ".");
            return Set.of();
        }

        var moves = currentBoard.validMoves(origin);
        if (moves == null || moves.isEmpty()) {
            System.out.println("No legal moves for " + square + ".");
            return Set.of();
        }

        return moves.stream()
                .flatMap(m -> Stream.of(m.getStartPosition(), m.getEndPosition()))
                .collect(Collectors.toSet());
    }
}
