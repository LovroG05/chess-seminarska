package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

/**
 * Razred Kralja ki razširi razred Figure
 */
public class King extends Figure {
    public King (int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.KING);
        setFenNotation(FigureFENNotation.K);
    }

    /**
     * Metoda specifična za razred King
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

        if (diffX==diffY && diffX == 1) return true;

        return (getCoordX() == new_x || getCoordY() == new_y) && ((diffX == 1) || (diffY == 1));
    }
}
