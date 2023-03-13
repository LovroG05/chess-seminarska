package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.enums.FigureName;
import ml.perchperkins.objects.enums.GameStatus;
//import ml.perchperkins.objects.io.GameUpdateOutput;
import ml.perchperkins.objects.figures.*;
import ml.perchperkins.objects.io.GameUpdate;
import ml.perchperkins.objects.io.NewMove;

import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class Game {
    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    @Setter
    private boolean whitesTurn = true;

    @Getter
    @Setter
    private boolean canPawnPromote = false;
//    @Getter
//    @Setter
//    private Player white = new Player(true);
//    @Getter
//    @Setter
//    private Player black = new Player(false);
    @Getter
    @Setter
    private List<Move> history = new ArrayList<Move>();

    private List<Figure> toEat = new ArrayList<>();
    @Getter
    @Setter
    private Figure[][] chessboard =  new Figure[8][8];

    public Game() {
        for (int i = 0; i < 8; i++) {
            chessboard[1][i] = new Pawn(i, 1, true);
        }

        chessboard[0][0] = new Rook(0, 0, true);
        chessboard[0][7] = new Rook(7, 0, true);

        chessboard[0][1] = new Knight(1, 0, true);
        chessboard[0][6] = new Knight(6, 0, true);

        chessboard[0][2] = new Bishop(2, 0, true);
        chessboard[0][5] = new Bishop(5, 0, true);

        chessboard[0][3] = new Queen(3, 0, true);
        chessboard[0][4] = new King(4, 0, true);

        for (int i = 0; i < 8; i++) {
            chessboard[6][i] = new Pawn(i, 6, false);
        }

        chessboard[7][0] = new Rook(0, 7, false);
        chessboard[7][7] = new Rook(7, 7, false);

        chessboard[7][1] = new Knight(1, 7, false);
        chessboard[7][6] = new Knight(6, 7, false);

        chessboard[7][2] = new Bishop(2, 7, false);
        chessboard[7][5] = new Bishop(5, 7, false);

        chessboard[7][3] = new Queen(3, 7, false);
        chessboard[7][4] = new King(4, 7, false);
    }

    /**
     * zgradi tabelo šahovnice
     *
     * @return Figure[][] tabela, ki predstavlja šahovnico
     *
     *
     */
//    public Figure[][] renderChessBoard() {
//        Figure[][] chessboard = new Figure[8][8];
//
//        for (int x = 0; x < 8; x++) {
//            for (int y = 0; y < 8; y++) {
//                chessboard[x][y] = null;
//            }
//        }
//
//        for (Figure figure : white.getFigures()) {
//            chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
//        }
//
//        for (Figure figure : black.getFigures()) {
//            chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
//        }
//
//        return chessboard;
//    }

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
//        Figure[][] chessboard = renderChessBoard();
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
        clearToEatList();
//        Figure[][] chessboard = renderChessBoard();

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

        chessboard[move.newy()][move.newx()] = figure;
        chessboard[figure.getCoordY()][figure.getCoordX()] = null;
        figure.move(move.newx(), move.newy());


        // shut the fuck up @everyone i just moved the figure ill move it back if this is wrong
        addToEatList(chessboard[move.newy()][move.newx()]);
        GameStatus gs = checkGameStatus(toEat);
        if (figure.isWhite()) {
            if ((gs == GameStatus.WHITE_CHECK) || (gs == GameStatus.WHITE_CHECKMATE)) {
                // return player to previous position, DON'T SAVE

                figure.move(move.x(), move.y());
                chessboard[move.newy()][move.newx()] = null;
                chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
                System.out.println("check w");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false);
            }
        } else {
            if ((gs == GameStatus.BLACK_CHECK) || (gs == GameStatus.BLACK_CHECKMATE)) {
                // return player to previous position, DON'T SAVE
                figure.move(move.x(), move.y());
                chessboard[move.newy()][move.newx()] = null;
                chessboard[figure.getCoordY()][figure.getCoordX()] = figure;
                System.out.println("check b");
                return new GameUpdate(renderFEN(), history, uuid.toString(), checkGameStatus(), false);
            }
        }

        // prehranjevanje figur
        // je namensko ZA preverjanjem ali premik povzroči šah, ker se drugače igralec, ki ga pojemo ne šteje
        for (Figure dead : toEat) {
            chessboard[dead.getCoordY()][dead.getCoordX()] = null;
        }

        // register move in history
        history.add(new Move(whitePlayer, move.x(), move.y(), move.newx(), move.newy(), figure));



        boolean pawnPromotion = ((figure.getName() == FigureName.PAWN) &&
                ((figure.isWhite() && figure.getCoordY() == 7) ||
                        (!figure.isWhite() && figure.getCoordY() == 0)));

        canPawnPromote = pawnPromotion;

        whitesTurn = !whitesTurn;

        gs = checkGameStatus();

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
        King king = (King) stream(chessboard).flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .filter(Figure::isWhite)
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }

        King finalKing = king;
        Figure[] farr = new Figure[]{getBlackFigures().stream()
                .filter(figure -> figure != finalKing &&
                        !toExclude.contains(figure) &&
                        figure.isValidMove(finalKing.getCoordX(), finalKing.getCoordY(), this)
                )
                .findAny()
                .orElse(null)};

        GameStatus gs = GameStatus.RUNNING;
        for (Figure f : farr) {
            if (f != null) {
                gs = GameStatus.WHITE_CHECK;
                break;
            }
        }

        if (gs == GameStatus.WHITE_CHECK) {
            if (!checkKingsMovement(king)) return GameStatus.WHITE_CHECKMATE;
            return gs;
        }

        // check for black checks
        king = (King) stream(chessboard).flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .filter(k -> !k.isWhite())
                .findAny()
                .orElse(null);
        if (king == null) {
            return GameStatus.WTF_KING_DISSAPEARED;
        }

        King finalKing1 = king;
        farr = new Figure[]{getWhiteFigures().stream()
                .filter(figure -> figure != finalKing1 &&
                        !toExclude.contains(figure) &&
                        figure.isValidMove(finalKing1.getCoordX(), finalKing1.getCoordY(), this)
                )
                .findAny()
                .orElse(null)};

        for (Figure f : farr) {
            if (f != null) {
                gs = GameStatus.BLACK_CHECK;
                break;
            }
        }

        if (gs == GameStatus.BLACK_CHECK) {
            if (!checkKingsMovement(king)) return GameStatus.BLACK_CHECKMATE;
            return gs;
        }


        // stalemate bi mogu prevert premike USEH figur igralca ne sam kralja js pač ne znam brt
        // za usak premik bi mogu prevert tut to metodo kakšn vpliv povzroči na outcome igre TODO IMPORTANT VERY VERY MUCHOS
        // preverjam za igralca k ma nasledn premik
        // loopam čez figure in čez celo polje, dokler ne najdem ene k se jo da premaknt
        boolean isthereone;
        if (isWhitesTurn()) {
            isthereone = hasAFigureThatCanMove(getBlackFigures());

        } else {
            isthereone = hasAFigureThatCanMove(getWhiteFigures());
        }
        if (!isthereone) return GameStatus.STALEMATE;

        return GameStatus.RUNNING;
    }

    /**
     * @param figures tabela figur
     * @return a ma figuro k se loh premakne
     */
    private boolean hasAFigureThatCanMove(List<Figure> figures) {
        boolean isthereone = false;
        for (Figure figure: figures) {
            if (figure != null) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (figure.isValidMove(i, j, this) && !figure.causesCheck(this)) {
                            isthereone = true;
                            break;
                        }
                    }
                    if (isthereone) break;
                }
                if (isthereone) break;
            }
        }
        return isthereone;
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
    public boolean checkKingsMovement(Figure king) {
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

    public List<Figure> getBlackFigures() {
        return stream(chessboard).flatMap(Arrays::stream).filter(Objects::nonNull).filter(figure -> !figure.isWhite()).collect(toList());
    }

    public List<Figure> getWhiteFigures() {
        return stream(chessboard).flatMap(Arrays::stream).filter(Objects::nonNull).filter(Figure::isWhite).collect(toList());
    }

}
