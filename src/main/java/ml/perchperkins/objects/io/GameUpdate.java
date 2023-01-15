package ml.perchperkins.objects.io;

import ml.perchperkins.objects.Move;
import ml.perchperkins.objects.enums.GameStatus;

import java.util.List;

public record GameUpdate(
        String fen,
        List<Move> history,
        String uuid,

        GameStatus status
        ) {
}
