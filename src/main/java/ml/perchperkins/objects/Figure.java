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

    public void move(int new_x, int new_y) {
        coordX = new_x;
        coordY = new_y;
    }
}
