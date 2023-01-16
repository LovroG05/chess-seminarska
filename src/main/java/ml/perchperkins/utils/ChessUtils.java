package ml.perchperkins.utils;

public class ChessUtils {
    public static int[] convertChessCoordsToInt(String chessCoords) {
        int[] result = new int[2];
        result[0] = chessCoords.charAt(0) - 'a';
        result[1] = Integer.parseInt(String.valueOf(chessCoords.charAt(1))) - 1;

        return result;
    }

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
