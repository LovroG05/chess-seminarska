package ml.perchperkins.objects;


/**
 * @param isWhites je igralec bel?
 * @param old_x začetni x
 * @param old_y začetni y
 * @param new_x nov x
 * @param new_y nov y
 * @param figure figura
 */
public record Move (
        boolean isWhites,
        int old_x,
        int old_y,
        int new_x,
        int new_y,
        Figure figure) {
}