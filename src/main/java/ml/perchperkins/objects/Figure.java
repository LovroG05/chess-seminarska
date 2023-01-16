package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

@Getter
@Setter
public class Figure {
    private boolean isWhite;
    private int coordX;
    private int coordY;
    private boolean hasMoved = false;
    private FigureName name = FigureName.FIGURE;

    private FigureFENNotation fenNotation = FigureFENNotation.F;

    public Figure(int x, int y, boolean white) {
        coordX = x;
        coordY = y;
        isWhite = white;
    }

    public boolean isValidMove(int new_x, int new_y, Figure[][] chessboard) {
        return false;
    }

    protected boolean isWalkable(int new_x, int new_y, Figure[][] chessboard) {
        if (coordX == new_x) {
            // walk the vertical line
            if (coordY > new_y) {
                for (int y = new_y; y < coordY; y++) {
                    System.out.println(y);
                    if (chessboard[y][coordX] != null) return false;
                }
            } else {
                for (int y = coordY+1; y < new_y; y++) {
                    System.out.println(y);
                    if (chessboard[y][coordX] != null) return false;
                }
            }

        }
        // check if y1 and y2 are equal
        else if (coordY == new_y) {
            // walk the horizontal line
            if (coordX > new_x) {
                for (int x = new_x; x < coordX; x++) {
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
                if (chessboard[y][x] != null) return false;
            }
        }



        return true;
    }

    public void move(int new_x, int new_y) {
        coordX = new_x;
        coordY = new_y;
        hasMoved = true;
    }
}
