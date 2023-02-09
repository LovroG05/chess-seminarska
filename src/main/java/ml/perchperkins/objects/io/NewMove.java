package ml.perchperkins.objects.io;

/**
 * @param x original x
 * @param y original y
 * @param newx nov x
 * @param newy nov y
 */
public record NewMove(
        int x,
        int y,
        int newx,
        int newy
) {
}
