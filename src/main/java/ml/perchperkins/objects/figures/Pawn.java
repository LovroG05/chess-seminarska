package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

public class Pawn extends Figure {
    public Pawn(int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.PAWN);
        setFenNotation(FigureFENNotation.P);
    }

    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false; // no move
        }

        if ((Math.abs(new_y - getCoordY()) == 1) && (Math.abs(new_x - getCoordX()) == 0)) {
            // distance is 1
            // checks if forward relative to colour and SPOT IS EMPTY BECAUSE IT CANNOT EAT FORWARD
            if (chessboard[new_y][new_x] == null) {
                if (isWhite()) {
                    if (getCoordY() < new_y) {
                        return true;
                    }
                } else {
                    if (getCoordY() > new_y) {
                        return true;
                    }
                }
            }
        }

        if ((Math.abs(getCoordY() - (new_y)) == 2) &&
                (Math.abs(getCoordX() - (new_x)) == 0) &&
                (getCoordY() == 1 || getCoordY() == 6) &&
                !isHasMoved()) {
            // distance is 2
            // checks if forward
            if (chessboard[new_y][new_x] == null) {
                if (isWhite()) {
                    return (getCoordY() < new_y) && isWalkable(new_x, new_y, chessboard);
                } else {
                    return (getCoordY() > new_y) && isWalkable(new_x, new_y, chessboard);
                }
            }
        }

        if ((Math.abs(getCoordX() - new_x) == 1) && (Math.abs(getCoordY() - new_y) == 1)) {
            // eating, diagonal by 1
            // removing of ene
            return (chessboard[new_y][new_x] != null) && (chessboard[new_y][new_x].isWhite() != isWhite());
        }

        return false;
    }
}
