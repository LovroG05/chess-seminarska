package ml.perchperkins.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.enums.FigureName;
import ml.perchperkins.objects.enums.GameStatus;
import ml.perchperkins.objects.io.GameUpdate;
import ml.perchperkins.objects.io.NewMove;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    @Setter
    private boolean whitesTurn = true;

    public Map<String, Player> players = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private List<Move> history = new ArrayList<Move>();

    public Game() {
        players.put("white", new Player(true));
        players.put("black", new Player(false));
    }

    public Figure[][] renderChessBoard() {
        Figure[][] chessboard = new Figure[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                chessboard[x][y] = null;
            }
        }

        for (Figure figure : players.get("white").getFigures()) {
            chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
        }

        for (Figure figure : players.get("black").getFigures()) {
            chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
        }


        return chessboard;
    }

    public String renderFEN() {
        // https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation

        Figure[][] chessboard = renderChessBoard();

        StringBuilder fen = new StringBuilder();

        for (int y = 7; y >= 0; y--) {
            int empty = 0;
            StringBuilder rankS = new StringBuilder();
            for (int x = 0; x < 8; x++) {
                Figure figure = chessboard[y][x];
                if (figure != null) {
                    String f = figure.getFenNotation().toString();
                    if (figure.isWhite()) {
                        f = f.toUpperCase();
                    } else {
                        f = f.toLowerCase();
                    }

                    // če je potrebno appenda empty in ga nastavi na 0
                    if (empty != 0) {
                        rankS.append(empty);
                        empty = 0;
                    }

                    // appenda figuro
                    rankS.append(f);
                } else {
                    // figure na tem mestu ni zato poveča empty
                    empty++;
                }
            }

            // appenda karkol je ostalo če je potrebno
            if (!rankS.isEmpty()) {
                fen.append(rankS);
            }
            if (empty != 0) fen.append(empty);

            if (y > 0) fen.append("/");
        }

        fen.append(" ");
        fen.append(whitesTurn ? "w" : "b");

        return fen.toString();
    }

    public GameUpdate makeMove(boolean whitePlayer, NewMove move) {
        Figure[][] chessboard = renderChessBoard();

        if (chessboard[move.y()][move.x()] == null) {
            System.out.println("no piece");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
        }

        Figure figure = chessboard[move.y()][move.x()];

        if (figure.isWhite() != whitePlayer) {
            System.out.println("wrong figure color");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus()); // move is invalid, no correct color figure is on start coords
        }

        if (figure.isWhite() != whitesTurn) {
            System.out.println("wrong player");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus()); // move is invalid, not players turn
        }

        if (!figure.isValidMove(move.newx(), move.newy(), chessboard)) {
            System.out.println("move invalid");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus()); // move is invalid by figure logic
        }

        figure.move(move.newx(), move.newy());


        // shut the fuck up @everyone i just moved the figure ill move it back if this is wrong
        List<Figure> l = new ArrayList<>();
        l.add(chessboard[move.newy()][move.newx()]);
        GameStatus gs = checkGameStatus(l);
        if (figure.isWhite()) {
            if ((gs == GameStatus.WHITE_CHECK) || (gs == GameStatus.WHITE_CHECKMATE)) {
                // return player to previous position, DONT SAVE
                figure.move(move.x(), move.y());
                System.out.println("check w");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
            }
        } else {
            if ((gs == GameStatus.BLACK_CHECK) || (gs == GameStatus.BLACK_CHECKMATE)) {
                // return player to previous position, DONT SAVE
                figure.move(move.x(), move.y());
                System.out.println("check b");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
            }
        }

        // prehranjevanje figur
        // je namensko ZA preverjanjom ali premik povzroči šah ker se drugače igralec ki ga pojemo ne šteje
        if (chessboard[move.newy()][move.newx()] != null) {
            if (chessboard[move.newy()][move.newx()].isWhite() != whitePlayer) {
                // opposite player's figure on coords, eat
                if (whitePlayer) {
                    players.get("black").getFigures().remove(chessboard[move.newy()][move.newx()]);
                } else {
                    players.get("white").getFigures().remove(chessboard[move.newy()][move.newx()]);
                }
            } else {
                System.out.println("same color in dest sq");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
            }
        }
        // register move in history
        history.add(new Move(whitePlayer, move.x(), move.y(), move.newx(), move.newy(), figure));

        whitesTurn = !whitesTurn;

        return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
    }

    public GameStatus checkGameStatus() {
        return checkGameStatus(new ArrayList<Figure>());
    }

    public GameStatus checkGameStatus(List<Figure> toExclude) {
        // checkmate, check, stalemate, running

        // check for checks
        // check for white checks
        Figure king = players.get("white").getFigures().stream()
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }
        Figure[][] chessboard = renderChessBoard();
        for (Figure figure : players.get("black").getFigures()) {
            if (!toExclude.contains(figure)) {
                if (figure.isValidMove(king.getCoordX(), king.getCoordY(), chessboard)) {
                    // check if the white king can move
                    if (!checkKingsMovement(king, chessboard)) {
                        // white king cant move, checkmate
                        return GameStatus.WHITE_CHECKMATE;
                    }
                    // white king in danger!
                    return GameStatus.WHITE_CHECK;
                }
            }
        }

        // stalemate
        if (!checkKingsMovement(king, chessboard)) return GameStatus.STALEMATE;

        // check for black checks
        king = players.get("black").getFigures().stream()
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }

        for (Figure figure : players.get("white").getFigures()) {
            if (!toExclude.contains(figure)) {
                if (figure.isValidMove(king.getCoordX(), king.getCoordY(), chessboard)) {
                    // check if the white king can move
                    if (!checkKingsMovement(king, chessboard)) {
                        // black king cant move, checkmate
                        return GameStatus.BLACK_CHECKMATE;
                    }
                    // black king in danger!
                    return GameStatus.BLACK_CHECK;
                }
            }
        }

        // stalemate
        if (!checkKingsMovement(king, chessboard)) return GameStatus.STALEMATE;

        return GameStatus.RUNNING;
    }

    private boolean checkKingsMovement(Figure king, Figure[][] chessboard) {
        // if true king can move
        return (king.isValidMove(king.getCoordX() + 1, king.getCoordY(), chessboard) ||
                king.isValidMove(king.getCoordX() - 1, king.getCoordY(), chessboard) ||
                king.isValidMove(king.getCoordX(), king.getCoordY() + 1, chessboard) ||
                king.isValidMove(king.getCoordX(), king.getCoordY() - 1, chessboard) ||
                king.isValidMove(king.getCoordX() + 1, king.getCoordY() + 1, chessboard) ||
                king.isValidMove(king.getCoordX() + 1, king.getCoordY() - 1, chessboard) ||
                king.isValidMove(king.getCoordX() - 1, king.getCoordY() + 1, chessboard) ||
                king.isValidMove(king.getCoordX() - 1, king.getCoordY() - 1, chessboard));
    }

    public void broadcastGameInfo() {

        players.entrySet().stream().filter(e -> e.getValue().user != null && e.getValue().user.isOpen()).forEach(entry -> {
            try {
                GameUpdate gup = new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
                ObjectMapper mapper = new ObjectMapper();
                System.out.println("sending to: " + entry.getValue().isWhite());
                entry.getValue().user.getRemote().sendString(mapper.writeValueAsString(gup));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
