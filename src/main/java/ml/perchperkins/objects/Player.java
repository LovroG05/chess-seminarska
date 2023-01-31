package ml.perchperkins.objects;

import lombok.Getter;
import lombok.Setter;
import ml.perchperkins.objects.figures.*;
import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.List;

public class Player {
    @Getter
    @Setter
    private boolean isWhite;
    @Getter
    @Setter
    private List<Figure> figures = new ArrayList<>();
    @Getter
    @Setter
    private boolean isConnected = false;
    public Session user = null;
    public Player(boolean white) {
        isWhite = white;

        if (isWhite) {
            for (int i = 0; i < 8; i++) {
                figures.add(new Pawn(i, 1, isWhite));
            }

            figures.add(new Rook(0, 0, isWhite));
            figures.add(new Rook(7, 0, isWhite));

            figures.add(new Knight(1, 0, isWhite));
            figures.add(new Knight(6, 0, isWhite));

            figures.add(new Bishop(2, 0, isWhite));
            figures.add(new Bishop(5, 0, isWhite));

            figures.add(new Queen(3, 0, isWhite));
            figures.add(new King(4, 0, isWhite));
        } else {
            for (int i = 0; i < 8; i++) {
                figures.add(new Pawn(i, 6, isWhite));
            }

            figures.add(new Rook(0, 7, isWhite));
            figures.add(new Rook(7, 7, isWhite));

            figures.add(new Knight(1, 7, isWhite));
            figures.add(new Knight(6, 7, isWhite));

            figures.add(new Bishop(2, 7, isWhite));
            figures.add(new Bishop(5, 7, isWhite));

            figures.add(new Queen(3, 7, isWhite));
            figures.add(new King(4, 7, isWhite));
        }
    }
}
