package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

public class Rook extends Figure {
    public Rook(int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.ROOK);
        setFenNotation(FigureFENNotation.R);
    }

    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        if (!(getCoordX() == new_x || getCoordY() == new_y)) return false;

        return isWalkable(new_x, new_y, chessboard);
    }
}
