package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class InGame extends Endpoint {
    private final Gson serializer;
    private int sessionId;
    private ChessGame currentGame;
    private ChessGame.TeamColor playerColor;
    private String username;
    private String token;
    private Session wsSession;

    public InGame(ChessGame gameInstance, ChessGame.TeamColor side, String userName, String authToken, int gameIdentifier) throws Exception {
        this.currentGame = gameInstance;
        this.playerColor = side;
        this.username = userName;
        this.token = authToken;
        this.sessionId = gameIdentifier;
        this.serializer = new Gson();
        this.wsSession = ContainerProvider.getWebSocketContainer().connectToServer(this, new URI("ws://localhost:8080/ws"));
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.wsSession = session;
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                processServerMessage(message);
            }
        });
    }

    public void beginSession() throws Exception {
        wsSession.getBasicRemote().sendText(serializer.toJson(new ConnectCommand(token, sessionId)));
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("[Status:In_Game] >>> ");
            String userInput = scanner.next().trim().toLowerCase();

            switch (userInput) {
                case "help":
                    printHelp();
                    break;

                case "redraw":
                    drawBoard(null);
                    break;

                case "leave":
                    wsSession.getBasicRemote().sendText(serializer.toJson(new LeaveCommand(token, sessionId)));
                    return;

                case "move":
                    if (playerColor != null) {
                        processMove(scanner);
                    } else {
                        printUnrecognizedCommand();
                    }
                    break;

                case "resign":
                    handleResign();
                    break;

                case "highlight":
                    handleHighlight(scanner);
                    break;

                default:
                    printUnrecognizedCommand();
            }
        }
    }

    private void printHelp() {
        if (playerColor != null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + """
                    move      <from> <to>
                    resign                """);
        }
        System.out.println("""
                    redraw
                    highlight <from>
                    leave
                    help""" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void printUnrecognizedCommand() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Command not recognized (type 'help')" + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleResign() {
        if (playerColor != null) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Confirm resign <y/n>: " + EscapeSequences.RESET_TEXT_COLOR);
            try {
                String answer = new Scanner(System.in).next();
                if (answer.toLowerCase().charAt(0) == 'y') {
                    wsSession.getBasicRemote().sendText(serializer.toJson(new ResignCommand(token, sessionId)));
                }
            } catch (Exception ignored) {
            }
        } else {
            printUnrecognizedCommand();
        }
    }

    private void handleHighlight(Scanner scanner) {
        try {
            String pos = scanner.next();
            ChessPosition position = convertToPosition(pos);
            if (currentGame.getBoard().getPiece(position) == null) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: No piece present" + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                drawBoard(position);
            }
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid usage" + EscapeSequences.RESET_TEXT_COLOR);
        }
    }
    private void processMove(Scanner scanner) {
        if (currentGame == null) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Board not loaded" + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        try {
            String from = scanner.next();
            String to = scanner.next();
            ChessPosition source = convertToPosition(from);
            ChessPosition destination = convertToPosition(to);
            ChessPiece.PieceType promotion = null;
            boolean isPawnPromotion = currentGame.getBoard().getPiece(source) != null
                    && currentGame.getBoard().getPiece(source).getPieceType() == ChessPiece.PieceType.PAWN
                    && (destination.getRow() == 1 || destination.getRow() == 8);
            if (isPawnPromotion) {
                promotion = selectPromotionPiece();
            }
            ChessMove move = new ChessMove(source, destination, promotion);
            String payload = serializer.toJson(new MakeMoveCommand(token, sessionId, move));
            wsSession.getBasicRemote().sendText(payload);
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid usage" + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private ChessPosition convertToPosition(String text) {
        try {
            ChessPosition pos = new ChessPosition(
                    text.charAt(1) - '0',
                    text.toLowerCase().charAt(0) - 'a' + 1
            );
            if (!pos.isValid()) {
                throw new RuntimeException(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid position" + EscapeSequences.RESET_TEXT_COLOR);
            }
            return pos;
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid syntax" + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private ChessPiece.PieceType selectPromotionPiece() {
        System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Enter pawn promotion <q/n/r/b>: " + EscapeSequences.RESET_TEXT_COLOR);
        Scanner scanner = new Scanner(System.in);
        char choice = scanner.next().toLowerCase().charAt(0);
        if (choice == 'q') {
            return ChessPiece.PieceType.QUEEN;
        } else if (choice == 'n') {
            return ChessPiece.PieceType.KNIGHT;
        } else if (choice == 'r') {
            return ChessPiece.PieceType.ROOK;
        } else if (choice == 'b') {
            return ChessPiece.PieceType.BISHOP;
        } else {
            throw new RuntimeException(EscapeSequences.SET_TEXT_COLOR_RED + "Error: Invalid piece" + EscapeSequences.RESET_TEXT_COLOR);
        }
    }
    private void addPieces(ChessPiece piece) {
        if (piece == null) {
            System.out.print("   ");
        } else {
            System.out.print(EscapeSequences.SET_TEXT_BOLD);
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
            } else {
                System.out.print(EscapeSequences.SET_TEXT_COLOR_RED);
            }
            switch (piece.getPieceType()) {
                case KING:
                    System.out.print(" K ");
                    break;
                case QUEEN:
                    System.out.print(" Q ");
                    break;
                case BISHOP:
                    System.out.print(" B ");
                    break;
                case KNIGHT:
                    System.out.print(" N ");
                    break;
                case ROOK:
                    System.out.print(" R ");
                    break;
                case PAWN:
                    System.out.print(" P ");
                    break;
            }
            System.out.print(EscapeSequences.RESET_TEXT_BOLD_FAINT);
        }
    }

    private void drawBoard(ChessPosition highlight) {
        System.out.println("");
        Collection<ChessMove> validMoves;
        if (highlight == null) {
            validMoves = new ArrayList<>();
        } else {
            validMoves = currentGame.validMoves(highlight);
            if (validMoves == null) {
                validMoves = new ArrayList<>();
            }
        }
        List<ChessPosition> targets = validMoves.stream().map(ChessMove::getEndPosition).toList();
        boolean flippedBoard;
        if (playerColor == ChessGame.TeamColor.BLACK) {
            flippedBoard = true;
        } else {
            flippedBoard = false;
        }
        makeBorders(flippedBoard);

        int rowStart;
        int rowEnd;
        int rowStep;
        if (flippedBoard) {
            rowStart = 0;
            rowEnd = 8;
            rowStep = 1;
        } else {
            rowStart = 7;
            rowEnd = -1;
            rowStep = -1;
        }
        for (int row = rowStart; row != rowEnd; row += rowStep) {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY
                    + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + (row + 1) + " ");
            int colStart;
            int colEnd;
            int colStep;
            if (flippedBoard) {
                colStart = 7;
                colEnd = -1;
                colStep = -1;
            } else {
                colStart = 0;
                colEnd = 8;
                colStep = 1;
            }
            for (int col = colStart; col != colEnd; col += colStep) {
                ChessPosition pos = new ChessPosition(row + 1, col + 1);
                if (pos.equals(highlight)) {
                    System.out.print(EscapeSequences.SET_BG_COLOR_YELLOW);
                } else {
                    if ((row + col) % 2 == 0) {
                        if (targets.contains(pos)) {
                            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                        } else {
                            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
                        }
                    } else {
                        if (targets.contains(pos)) {
                            System.out.print(EscapeSequences.SET_BG_COLOR_GREEN);
                        } else {
                            System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
                        }
                    }
                }
                ChessPiece piece = currentGame.getBoard().getPiece(pos);
                addPieces(piece);
            }
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY
                    + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + (row + 1) + " ");
            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }

        makeBorders(flippedBoard);
    }

    private void makeBorders(boolean flipped) {
        String labels;
        if (flipped) {
            labels = "hgfedcba";
        } else {
            labels = "abcdefgh";
        }
        System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY
                + EscapeSequences.SET_TEXT_COLOR_WHITE + "   ");
        for (int i = 0; i < 8; i++) {
            System.out.print(" " + labels.charAt(i) + " ");
        }
        System.out.println("   " + EscapeSequences.RESET_BG_COLOR
                + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void processServerMessage(String json) {
        ServerMessage msg = serializer.fromJson(json, ServerMessage.class);
        switch (msg.getServerMessageType()) {
            case NOTIFICATION -> {
                String note = serializer.fromJson(json, NotificationMessage.class).getMessage();
                System.out.println("\r" + EscapeSequences.ERASE_LINE + EscapeSequences.SET_TEXT_COLOR_MAGENTA + note +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
            case ERROR -> {
                String error = serializer.fromJson(json, ErrorMessage.class).getErrorMessage();
                System.out.println("\r" + EscapeSequences.ERASE_LINE + EscapeSequences.SET_TEXT_COLOR_RED + error +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
            case LOAD_GAME -> {
                LoadGameMessage loadMsg = serializer.fromJson(json, LoadGameMessage.class);
                this.currentGame = loadMsg.getGame();
                System.out.print("\r" + EscapeSequences.ERASE_LINE);
                drawBoard(null);
            }
        }
        System.out.print("[Status:In_Game] >>> ");
    }
}
