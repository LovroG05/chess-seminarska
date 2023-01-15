package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

public class Bishop extends Figure{
    public Bishop (int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.BISHOP);
        setFenNotation(FigureFENNotation.B);
    }

    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        int diffX = Math.abs(getCoordX() -new_x);
        int diffY = Math.abs(getCoordY() -new_y);

        return diffX == diffY;
    }
}
