package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.enums.FigureName;
import ml.perchperkins.objects.enums.GameStatus;
//import ml.perchperkins.objects.io.GameUpdateOutput;
import ml.perchperkins.objects.figures.King;
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
    private boolean canPawnPromote = false;
    @Getter
    @Setter
    private Player white = new Player(true);
    @Getter
    @Setter
    private Player black = new Player(false);
    @Getter
    @Setter
    private List<Move> history = new ArrayList<Move>();

    private List<Figure> toEat = new ArrayList<>();

    /**
     * zgradi tabelo šahovnice
     *
     * @return Figure[][] tabela, ki predstavlja šahovnico
     *
     *
     */
    public Figure[][] renderChessBoard() {
        Figure[][] chessboard = new Figure[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                chessboard[x][y] = null;
            }
        }

        for (Figure figure : white.getFigures()) {
            chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
        }

        for (Figure figure : black.getFigures()) {
            chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
        }

        return chessboard;
    }

    /**
     * zgradi FEN string iz šahovnice
     * <a href="https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation">...</a>
     * implementacija ne vklučuje polj za castling, en passant, halfmove clock in fullmove number
     *
     * @return String FEN string
     *
     */
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

    /**
     * @param whitePlayer je igralec bel?
     * @param move objekt NewMove, ki opiše premik
     * @return GameUpdate
     */
    public GameUpdate makeMove(boolean whitePlayer, NewMove move) {
        System.out.println("---------- new move ------------");
        clearToEatList();
        Figure[][] chessboard = renderChessBoard();

        if (chessboard[move.y()][move.x()] == null) {
            System.out.println("no piece");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false);
        }

        Figure figure = chessboard[move.y()][move.x()];

        if (figure.isWhite() != whitePlayer) {
            System.out.println("wrong figure color");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false); // move is invalid, no correct color figure is on start coords
        }

        if (figure.isWhite() != whitesTurn) {
            System.out.println("wrong player");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false); // move is invalid, not players turn
        }

        if (!figure.isValidMove(move.newx(), move.newy(), this)) {
            System.out.println("move invalid");
            return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false); // move is invalid by figure logic
        }

        figure.move(move.newx(), move.newy());


        // shut the fuck up @everyone i just moved the figure ill move it back if this is wrong
        addToEatList(chessboard[move.newy()][move.newx()]);
        GameStatus gs = checkGameStatus(toEat);
        if (figure.isWhite()) {
            if ((gs == GameStatus.WHITE_CHECK) || (gs == GameStatus.WHITE_CHECKMATE)) {
                // return player to previous position, DON'T SAVE
                figure.move(move.x(), move.y());
                System.out.println("check w");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false);
            }
        } else {
            if ((gs == GameStatus.BLACK_CHECK) || (gs == GameStatus.BLACK_CHECKMATE)) {
                // return player to previous position, DON'T SAVE
                figure.move(move.x(), move.y());
                System.out.println("check b");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false);
            }
        }

        // prehranjevanje figur
        // je namensko ZA preverjanjom ali premik povzroči šah ker se drugače igralec ki ga pojemo ne šteje
        for (Figure dead : toEat) {
            if (dead.isWhite()) {
                white.getFigures().remove(dead);
            } else {
                black.getFigures().remove(dead);
            }
        }

        // register move in history
        history.add(new Move(whitePlayer, move.x(), move.y(), move.newx(), move.newy(), figure));



        boolean pawnPromotion = ((figure.getName() == FigureName.PAWN) &&
                ((figure.isWhite() && figure.getCoordY() == 7) ||
                        (!figure.isWhite() && figure.getCoordY() == 0)));

        canPawnPromote = pawnPromotion;

        gs = checkGameStatus();

        whitesTurn = !whitesTurn;

        return new GameUpdate(renderFEN(), history, uuid.toString(), gs, pawnPromotion);
    }

    /**
     * Metoda je namenjena preverjanju statusa igre (šah, šah mat, stalemate)
     *
     * @return GameStatus status igre v enumu
     *
     *
     */
    public GameStatus checkGameStatus() {
        return checkGameStatus(new ArrayList<Figure>());
    }

    /**
     * Metoda je namenjena preverjanju statusa igre (šah, šah mat, stalemate)
     *
     * @param toExclude figure, ki jih ne želimo upoštevati
     * @return GameStatus status igre v enumu
     *
     *
     */
    public GameStatus checkGameStatus(List<Figure> toExclude) {
        // checkmate, check, stalemate, running

        // check for checks
        // for white checks
//        Figure[][] chessboard = renderChessBoard();
        System.out.println("check game status");
        Figure king = white.getFigures().stream()
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }
        for (Figure figure : black.getFigures()) {
            if (!toExclude.contains(figure)) {
                if (figure.isValidMove(king.getCoordX(), king.getCoordY(), this)) {
                    // check if the white king can move
                    if (!checkKingsMovement(king)) {
                        // white king cant move, checkmate
                        return GameStatus.WHITE_CHECKMATE;
                    }
                    // white king in danger!
                    return GameStatus.WHITE_CHECK;
                }
            }
        }

        // check for black checks
        king = black.getFigures().stream()
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }

        for (Figure figure : white.getFigures()) {
            if (!toExclude.contains(figure)) {
                if (figure.isValidMove(king.getCoordX(), king.getCoordY(), this)) {
                    // check if the white king can move
                    if (!checkKingsMovement(king)) {
                        // black king cant move, checkmate
                        return GameStatus.BLACK_CHECKMATE;
                    }
                    // black king in danger!
                    return GameStatus.BLACK_CHECK;
                }
            }
        }

        // TODO stalemate bi mogu prevert premike USEH figur igralca ne sam kralja js pač ne znam brt
        // preverjam za igralca k ma nasledn premik
        // loopam čez figure in čez celo polje dokler ne najdem ene k se jo da premaknt
        if (isWhitesTurn()) {
            boolean isthereone = false;
            for (Figure figure: black.getFigures()) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (figure.isValidMove(i, j, this)) {
                            isthereone = true;
                            break;
                        }
                    }
                    if (isthereone) break;
                }
                if (isthereone) break;
            }

            if (!isthereone) return GameStatus.STALEMATE;
        } else {
            boolean isthereone = false;
            for (Figure figure: white.getFigures()) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (figure.isValidMove(i, j, this)) {
                            isthereone = true;
                            break;
                        }
                    }
                    if (isthereone) break;
                }
                if (isthereone) break;
            }

            if (!isthereone) return GameStatus.STALEMATE;
        }

        return GameStatus.RUNNING;
    }

    /**
     * @param figure Figura, ki je na poti v grob
     */
    public void addToEatList(Figure figure) {
        if (figure != null && figure.isWhite() != whitesTurn) {
            toEat.add(figure);
        }
    }

    /**
     * pobriše list za pojest
     */
    private void clearToEatList() {
        toEat.clear();
    }

    /**
     * @param king Figura kralja
     * @return boolean true, če se kralj lahko premakne
     */
    private boolean checkKingsMovement(Figure king) {
        boolean step1 = king.isValidMove(king.getCoordX() + 1, king.getCoordY(), this);
        boolean step2 = king.isValidMove(king.getCoordX() - 1, king.getCoordY(), this);
        boolean step3 = king.isValidMove(king.getCoordX(), king.getCoordY() + 1, this);
        boolean step4 = king.isValidMove(king.getCoordX(), king.getCoordY() - 1, this);
        boolean step5 = king.isValidMove(king.getCoordX() + 1, king.getCoordY() + 1, this);
        boolean step6 = king.isValidMove(king.getCoordX() + 1, king.getCoordY() - 1, this);
        boolean step7 = king.isValidMove(king.getCoordX() - 1, king.getCoordY() + 1, this);
        boolean step8 = king.isValidMove(king.getCoordX() - 1, king.getCoordY() - 1, this);
        // if true king can move
        return (step1 ||
                step2 ||
                step3 ||
                step4 ||
                step5 ||
                step6 ||
                step7 ||
                step8);
    }

    public void addToHistory(Move move) {
        history.add(move);
    }
}
