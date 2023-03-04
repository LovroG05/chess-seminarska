package ml.perchperkins.objects.figures;

import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.Move;
import ml.perchperkins.objects.enums.FigureFENNotation;
import ml.perchperkins.objects.enums.FigureName;

import java.util.List;

/**
 * Razred kmeta ki razširi razred Figure
 */
public class Pawn extends Figure {
    public Pawn(int x, int y, boolean white) {
        super(x, y, white);
        setName(FigureName.PAWN);
        setFenNotation(FigureFENNotation.P);
    }

    /**
     * Metoda specifična za razred Pawn
     *
     * @param new_x nov X premika
     * @param new_y nov Y premika
     * @return boolean true, če je premik možen
     *
     *
     */
    public boolean isValidMove(int new_x, int new_y, Game game) {
        if (getCoordX() == new_x && getCoordY() == new_y) {
            return false; // no move
        }

        if ((Math.abs(new_y - getCoordY()) == 1) && (Math.abs(new_x - getCoordX()) == 0)) {
            // distance is 1
            // checks if forward relative to colour and SPOT IS EMPTY BECAUSE IT CANNOT EAT FORWARD
            if (game.getChessboard()[new_y][new_x] == null) {
                if (isWhite()) {
                    if (getCoordY() < new_y) {
                        return true;
                    }
                } else {
                    if (getCoordY() > new_y) {
                        return true;
                    }
                }
            }
        }

        if ((Math.abs(getCoordY() - (new_y)) == 2) &&
                (Math.abs(getCoordX() - (new_x)) == 0) &&
                (getCoordY() == 1 || getCoordY() == 6) &&
                getNOfMoves() == 0) {
            // distance is 2
            // checks if forward
            if (game.getChessboard()[new_y][new_x] == null) {
                if (isWhite()) {
                    return (getCoordY() < new_y) && isWalkable(new_x, new_y, game.getChessboard());
                } else {
                    return (getCoordY() > new_y) && isWalkable(new_x, new_y, game.getChessboard());
                }
            }
        }

        if ((Math.abs(getCoordX() - new_x) == 1) && (Math.abs(getCoordY() - new_y) == 1)) {
            // eating, diagonal by 1
            if ((game.getChessboard()[new_y][new_x] != null) && (game.getChessboard()[new_y][new_x].isWhite() != isWhite())) return true;

            // en passant
            if (isWhite()) {
                if (getCoordY() == 4) {
                    // The capturing pawn must have advanced exactly three ranks to perform this move
                    if (game.getChessboard()[new_y-1][new_x].getName() == FigureName.PAWN) {
                        Figure opposingPawn = game.getChessboard()[new_y-1][new_x];
                        if (opposingPawn.getNOfMoves() == 1) {
                            if (game.getHistory().get(game.getHistory().size()-1).figure() == opposingPawn) {
                                // en passant possible
                                game.addToEatList(opposingPawn);
                                return true;
                            }
                        }
                    }
                }
            } else {
                if (getCoordY() == 3) {
                    // The capturing pawn must have advanced exactly three ranks to perform this move
                    if (game.getChessboard()[new_y+1][new_x].getName() == FigureName.PAWN) {
                        Figure opposingPawn = game.getChessboard()[new_y+1][new_x];
                        if (opposingPawn.getNOfMoves() == 1) {
                            if (game.getHistory().get(game.getHistory().size()-1).figure() == opposingPawn) {
                                // en passant possible
                                game.addToEatList(opposingPawn);
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
