package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

public class King extends Figure {
    public King (int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.KING);
        setFenNotation(FigureFENNotation.K);
    }

    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        int diffX = Math.abs(getCoordX() -new_x);
        int diffY = Math.abs(getCoordY() -new_y);

        if (diffX==diffY && diffX == 1) return true;

        // verify that king's new coords are safe
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((chessboard[i][j] != null) && (chessboard[i][j].isWhite() != isWhite())) {
                    if (!chessboard[i][j].isValidMove(new_x, new_y, chessboard)) {
                        return false;
                    }
                }
            }
        }

        if (!((getCoordX() == new_x || getCoordY() == new_y) && diffX <= 1)) return false;

        return isWalkable(new_x, new_y, chessboard);
    }
}
