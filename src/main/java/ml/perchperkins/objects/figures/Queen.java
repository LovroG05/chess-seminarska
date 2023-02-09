package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

/**
 * Razred Kraljice ki razširi razred Figure
 */
public class Queen extends Figure {
    public Queen(int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.QUEEN);
        setFenNotation(FigureFENNotation.Q);
    }

    /**
     * Metoda specifična za razred Queen
     *
     * @param new_x nov X premika
     * @param new_y nov Y premika
     * @param chessboard tabela šahovnice
     * @return boolean true če je premik možen
     *
     *
     */
    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        int diffX = Math.abs(getCoordX() - new_x);
        int diffY = Math.abs(getCoordY() - new_y);

        return (((diffX == diffY) || (getCoordX() == new_x || getCoordY() == new_y)) && isWalkable(new_x, new_y, chessboard));
    }
}
