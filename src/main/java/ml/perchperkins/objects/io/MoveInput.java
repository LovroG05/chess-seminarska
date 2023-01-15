package ml.perchperkins.objects.io;

public record MoveInput(
        String source,
        String target,
        String piece,
        String orientation
) {
}
