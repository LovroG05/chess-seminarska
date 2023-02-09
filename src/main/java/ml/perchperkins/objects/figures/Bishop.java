package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

/**
 * Figura Lovca, ki razširi razred Figure
 */
public class Bishop extends Figure{
    public Bishop (int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.BISHOP);
        setFenNotation(FigureFENNotation.B);
    }

    /**
     * Metoda specifična za razred Bishop
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

        int diffX = Math.abs(getCoordX() -new_x);
        int diffY = Math.abs(getCoordY() -new_y);

        if (diffX != diffY) {
            return false;
        }

        return isWalkable(new_x, new_y, chessboard);
    }
}
