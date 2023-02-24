package ml.perchperkins.objects.io;

import ml.perchperkins.objects.Move;
import ml.perchperkins.objects.enums.GameStatus;

import java.util.List;

/**
 * Record namenjen objektu, ki ga vmesniku vrne strežnik po premiku.
 *
 * @param fen FEN string
 * @param history List zgodovine premikov
 * @param uuid UUID igre
 * @param status status igre
 * @param pawnPromotion true če lahko igralec, ki je zadnji naredil premik zamenja figuro
 *
 */
public record GameUpdate(
        String fen,
        List<Move> history,
        String uuid,
        GameStatus status,
        boolean pawnPromotion
        ) {
}
