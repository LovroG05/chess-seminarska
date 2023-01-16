package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

public class Queen extends Figure {
    public Queen(int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.QUEEN);
        setFenNotation(FigureFENNotation.Q);
    }

    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        int diffX = Math.abs(getCoordX() - new_x);
        int diffY = Math.abs(getCoordY() - new_y);

        if (diffX == diffY) return true;

        if (!(getCoordX() == new_x || getCoordY() == new_y)) return false;
        return isWalkable(new_x, new_y, chessboard);
    }
}
