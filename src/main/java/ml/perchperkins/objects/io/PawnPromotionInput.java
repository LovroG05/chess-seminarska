package ml.perchperkins.objects.io;

public record PawnPromotionInput(
        int x,
        int y,
        boolean isWhite,
        String newFigureFEN
) {
}
