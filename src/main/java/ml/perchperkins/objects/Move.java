package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Move {
    private boolean isWhites;
    private int old_x;
    private int old_y;
    private int new_x;
    private int new_y;
    private Figure figure;
    public Move(boolean _white, int _old_x, int _old_y, int _new_x, int _new_y, Figure _figure) {
        isWhites = _white;
        old_x = _old_x;
        old_y = _old_y;
        new_x = _new_x;
        new_y = _new_y;

        figure = _figure;
    }
}
