package ml.perchperkins.utils;

public class ChessUtils {
    public static int[] convertChessCoordsToInt(String chessCoords) {
        int[] result = new int[2];
        result[0] = chessCoords.charAt(0) - 'a';
        result[1] = Integer.parseInt(String.valueOf(chessCoords.charAt(1))) - 1;

        return result;
    }
}
