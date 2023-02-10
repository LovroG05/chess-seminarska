package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.Move;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

import java.util.List;

/**
 * Razred Skakača ki razširi razred Figure
 */
public class Knight extends Figure {
    public Knight(int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.KNIGHT);
        setFenNotation(FigureFENNotation.N);
    }

    /**
     * Metoda specifična za razred Knight
     *
     * @param new_x nov X premika
     * @param new_y nov Y premika
     * @return boolean true, če je premik možen
     *
     *
     */
    public boolean isValidMove(int new_x, int new_y, Game game) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        int diffX = Math.abs(getCoordX() - new_x);
        int diffY = Math.abs(getCoordY() - new_y);

        return (diffY + diffX) == 3 && diffX != 0 && diffY != 0;
    }
}
