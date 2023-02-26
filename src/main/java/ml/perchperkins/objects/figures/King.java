package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.Move;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

import java.util.List;

/**
 * Razred Kralja ki razširi razred Figure
 */
public class King extends Figure {
    public King (int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.KING);
        setFenNotation(FigureFENNotation.K);
    }

    /**
     * Metoda specifična za razred King
     *
     * @param new_x nov X premika
     * @param new_y nov Y premika
     * @return boolean true če je premik možen
     *
     *
     */
    public boolean isValidMove(int new_x, int new_y, Game game) {
        if (new_x < 0 || new_x > 7 || new_y < 0 || new_y > 7) return false;
        Figure[][] chessboard = game.renderChessBoard();

        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false;
        }

        if (chessboard[new_y][new_x] != null && chessboard[new_y][new_x].isWhite() == isWhite()) return false;

        int diffX = Math.abs(getCoordX() -new_x);
        int diffY = Math.abs(getCoordY() -new_y);

        // rošada al neki
        if (diffX == 2 && getNOfMoves() == 0) {
            if (new_x > getCoordX()) {
                // castle u desno >>>>>
                Figure rook = chessboard[getCoordY()][7];
                if (rook.getNOfMoves() == 0) {
                    if (isWalkable(new_x, new_y, chessboard) && rook.isValidMove(new_x-1, new_y, game)) {
                        Move move = new Move(rook.isWhite(), rook.getCoordX(), rook.getCoordY(), new_x-1, new_y, rook);
                        rook.move(new_x-1, new_y);
                        game.addToHistory(move);
                        return true;
                    }
                }
            } else {
                // castle u desno <<<<<
                Figure rook = chessboard[getCoordY()][0];
                if (rook.getNOfMoves() == 0) {
                    if (isWalkable(new_x, new_y, chessboard) && rook.isValidMove(new_x+1, new_y, game)) {
                        Move move = new Move(rook.isWhite(), rook.getCoordX(), rook.getCoordY(), new_x+1, new_y, rook);
                        rook.move(new_x+1, new_y);
                        game.addToHistory(move);
                        return true;
                    }
                }
            }
        }

        if (diffX==diffY && diffX == 1) return true;

        return (getCoordX() == new_x || getCoordY() == new_y) && ((diffX == 1) || (diffY == 1));
    }
}
