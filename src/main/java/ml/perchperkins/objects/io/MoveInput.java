package ml.perchperkins.objects.io;

/**
 * @param source originalna koordinata (a5)
 * @param target ciljna koordinata (b6)
 * @param piece
 * @param orientation
 */
public record MoveInput(
        String source,
        String target,
        String piece,
        String orientation
) {
}
