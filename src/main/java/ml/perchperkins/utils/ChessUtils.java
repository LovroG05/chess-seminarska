package ml.perchperkins.utils;

/**
 * pomožni razred (zadeve k nism vedu kam bi jih dal
 */
public class ChessUtils {
    /**
     * metoda pretvori koordinate kot so a5 v int[] koordinat
     *
     * @param chessCoords koordinate kot so vidne na šahovnici primer: (a5)
     * @return int[] primer: [1, 4]
     *
     *
     */
    public static int[] convertChessCoordsToInt(String chessCoords) {
        int[] result = new int[2];
        result[0] = chessCoords.charAt(0) - 'a';
        result[1] = Integer.parseInt(String.valueOf(chessCoords.charAt(1))) - 1;

        return result;
    }

    /**
     * metoda namenjena debugganju metode renderFEN razreda Game
     *
     * @param fen FEN string
     * @return boolean true če je FEN string validen
     *
     *
     */
    public static boolean FENverifier(String fen) {
        String[] rankList = fen.split("/");

        if (rankList.length == 8) {
            for (String rank : rankList) {
                int rank_length = 0;
                for (int i = 0; i < rank.length(); i++) {
                    try {
                        int val = Integer.parseInt(String.valueOf(rank.charAt(i)));
                        rank_length += val;
                    } catch (NumberFormatException e) {
                        rank_length++;
                    }
                }

                if (rank_length != 8) return false;
            }
        } else {
            return false;
        }

        return true;
    }
}
