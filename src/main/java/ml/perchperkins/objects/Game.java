package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.enums.FigureName;
import ml.perchperkins.objects.enums.GameStatus;
//import ml.perchperkins.objects.io.GameUpdateOutput;
import ml.perchperkins.objects.io.GameUpdate;
import ml.perchperkins.objects.io.NewMove;
import ml.perchperkins.utils.ChessUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    @Setter
    private boolean whitesTurn = true;
    @Getter
    @Setter
    private Player white = new Player(true);
    @Getter
    @Setter
    private Player black = new Player(false);
    @Getter
    @Setter
    private List<Move> history = new ArrayList<Move>();

    public Figure[][] renderChessBoard() {
        Figure[][] chessboard = new Figure[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessboard[i][j] = null;
            }
        }

        for (Figure figure : white.getFigures()) {
            chessboard[figure.getCoordX()][figure.getCoordY()] = figure;
        }

        for (Figure figure : black.getFigures()) {
            chessboard[figure.getCoordX()][figure.getCoordY()] = figure;
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
                Figure figure = chessboard[x][y];
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

        if (chessboard[move.x()][move.y()] == null) {
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
        }

        Figure figure = chessboard[move.x()][move.y()];

        if (figure.isWhite() != whitePlayer) {
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus()); // move is invalid, no correct color figure is on start coords
        }

        if (figure.isWhite() != whitesTurn) {
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus()); // move is invalid, not players turn
        }

        if (!figure.isValidMove(move.newx(), move.newy(), chessboard)) {
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus()); // move is invalid by figure logic
        }

        if (chessboard[move.newx()][move.newy()] != null) {
            if (chessboard[move.newx()][move.newy()].isWhite() != whitePlayer) {
                // opposite player's figure on coords, eat
                if (whitePlayer) {
                    black.getFigures().remove(chessboard[move.newx()][move.newy()]);
                } else {
                    white.getFigures().remove(chessboard[move.newx()][move.newy()]);
                }
            } else {
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
            }
        }

        figure.move(move.newx(), move.newy());
        // register move in history
        history.add(new Move(whitePlayer, move.x(), move.y(), move.newx(), move.newy(), figure));

        whitesTurn = !whitesTurn;

        return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus());
    }

    public GameStatus checkGameStatus() {
        // checkmate, check, stalemate, running

        // check for checks
        // check for white checks
        Figure king = white.getFigures().stream()
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }
        Figure[][] chessboard = renderChessBoard();
        for (Figure figure : black.getFigures()) {
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

        // stalemate
        if (checkKingsMovement(king, chessboard)) return GameStatus.STALEMATE;

        // check for black checks
        king = black.getFigures().stream()
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }

        for (Figure figure : white.getFigures()) {
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

        // stalemate
        if (checkKingsMovement(king, chessboard)) return GameStatus.STALEMATE;

        return GameStatus.RUNNING;
    }

    private boolean checkKingsMovement(Figure king, Figure[][] chessboard) {
        // if true king can move
        return (!king.isValidMove(king.getCoordX() + 1, king.getCoordY(), chessboard) &&
                !king.isValidMove(king.getCoordX() - 1, king.getCoordY(), chessboard) &&
                !king.isValidMove(king.getCoordX(), king.getCoordY() + 1, chessboard) &&
                !king.isValidMove(king.getCoordX(), king.getCoordY() - 1, chessboard) &&
                !king.isValidMove(king.getCoordX() + 1, king.getCoordY() + 1, chessboard) &&
                !king.isValidMove(king.getCoordX() + 1, king.getCoordY() - 1, chessboard) &&
                !king.isValidMove(king.getCoordX() - 1, king.getCoordY() + 1, chessboard) &&
                !king.isValidMove(king.getCoordX() - 1, king.getCoordY() - 1, chessboard));
    }

/*    public void start() {
        if (white.isConnected() && black.isConnected()) {
            hasStarted = true;
            // the game should be started by the last player to join
            // start message is just the first game update
            GameUpdateOutput out = new GameUpdateOutput(renderChessBoard(), history, false, whitesTurn, checkGameStatus());
            try {
                ObjectMapper mapper = new ObjectMapper();
                String out_s = mapper.writeValueAsString(out);
                white.getUser().getRemote().sendString(out_s);
                black.getUser().getRemote().sendString(out_s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
}
