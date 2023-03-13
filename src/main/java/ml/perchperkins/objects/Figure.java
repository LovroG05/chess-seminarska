package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;
import ml.perchperkins.objects.enums.GameStatus;
import ml.perchperkins.objects.figures.King;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Razred Figure predstavlja šahovsko figuro
 */
@Getter
@Setter
public class Figure {
    private boolean isWhite;
    private int coordX;
    private int coordY;
//    private boolean hasMoved = false;
    private int nOfMoves = 0;
    private FigureName name = FigureName.FIGURE;

    private FigureFENNotation fenNotation = FigureFENNotation.F;

    public Figure(int x, int y, boolean white) {
        coordX = x;
        coordY = y;
        isWhite = white;
    }

    /**
     * metoda v tem razredu ni implementirana, saj je to osnovni razred figur.
     *
     * @param new_x nov x premika
     * @param new_y nov y premika
     * @param game instanca igre
     * @return true če je premik možen
     *
     *
     */
    public boolean isValidMove(int new_x, int new_y, Game game) {
        return false;
    }

    /**
     * metoda preverja, če je na poti figure kakšna druga figura, ki bi ji pot blokirala
     *
     * @param new_x nov x premika
     * @param new_y nov y premika
     * @param chessboard tabela šahovnice
     * @return true če je premik tja možen
     *
     *
     */
    protected boolean isWalkable(int new_x, int new_y, Figure[][] chessboard) {
        if ((new_x < 0 || new_y < 0) || (new_x > 7 || new_y > 7)) return false; // ponavad pomaga če figura ne more vn iz polja
        int diffX = Math.abs(getCoordX() - new_x);
        int diffY = Math.abs(getCoordY() - new_y);
        if ((diffX == 0) && (diffY == 0)) return true; // nauč se razmišlat....
        if ((diffX == 1) || (diffY == 1)) return true; // če se premakne za eno ne rabiš preverjat če je čez koga skoču

        if (coordX == new_x) {
            // walk the vertical line
            if (coordY > new_y) {
                for (int y = new_y+1; y < coordY; y++) {
                    if (chessboard[y][coordX] != null) return false;
                }
            } else {
                for (int y = coordY+1; y < new_y; y++) {
                    if (chessboard[y][coordX] != null) return false;
                }
            }

        }
        else if (coordY == new_y) {
            // walk the horizontal line
            if (coordX > new_x) {
                for (int x = new_x+1; x < coordX; x++) {
                    if (chessboard[coordY][x] != null) return false;
                }
            } else {
                for (int x = coordX+1; x < new_x; x++) {
                    if (chessboard[coordY][x] != null) return false;
                }
            }

        } else {
            // walk the diagonal line
            for (int i = 1; i <= Math.abs(new_x - coordX); i++) {
                int x = coordX + i;
                if (coordX > new_x) x = coordX - i;
                int y = coordY + i;
                if (coordY > new_y) y = coordY - i;
                if ((y >= 0 && y <= 7) && (x >= 0 && x <= 7)) {
                    if ((chessboard[y][x] != null) && (x != new_x && y != new_y)) return false;
                }
            }
        }

        return true;
    }

    /**
     * Metoda naredi premik (nastavi koordinate figure) in nastavi hasMoved na true
     *
     * @param new_x nov x
     * @param new_y nov y
     *
     *
     */
    public void move(int new_x, int new_y) {
        coordX = new_x;
        coordY = new_y;
        nOfMoves++;
    }

    public boolean causesCheck(Game game) {
        King king = (King) stream(game.getChessboard()).flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(figure -> FigureName.KING.equals(figure.getName()))
                .filter(figure -> figure.isWhite() == this.isWhite())
                .findAny()
                .orElse(null);
        if (king == null) {
            return true;
        }

        List<Figure> list = new ArrayList<>();
        for (Figure figure : (this.isWhite() ? game.getBlackFigures() : game.getWhiteFigures())) {
            if (figure != null &&
                    figure != king &&
                    figure != this &&
                    figure.isValidMove(king.getCoordX(), king.getCoordY(), game)) {
                list.add(figure);
            }
        }

        return list.size() == 0;
    }
}
