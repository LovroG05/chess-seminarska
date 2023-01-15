package ml.perchperkins.objects.io;

import ml.perchperkins.objects.Move;

import java.util.List;

public record GameUpdate(
        String fen,

        List<Move> history


        ) {
}
