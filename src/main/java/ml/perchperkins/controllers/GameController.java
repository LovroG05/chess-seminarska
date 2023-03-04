package ml.perchperkins.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import ml.perchperkins.objects.Figure;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.enums.GameStatus;
import ml.perchperkins.objects.figures.*;
import ml.perchperkins.objects.io.GameUpdate;
import ml.perchperkins.objects.io.MoveInput;
import ml.perchperkins.objects.io.NewMove;
import ml.perchperkins.objects.io.PawnPromotionInput;
import ml.perchperkins.utils.ChessUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ml.perchperkins.objects.enums.FigureFENNotation;

public class GameController {
    @Getter
    private static List<Game> games = new ArrayList<>();

    /**
     * Konstruktor razreda
     * definira poti za spletni strežnik, prilagodi CORS
     */
    public GameController() {
        enableCORS("*", "*", "*");
        Spark.path("/g", ()->{
           Spark.get("/new", this::newGame);
           Spark.post("/*/move", this::move);
           Spark.get("/*/chessboard", this::getChessboard);
           Spark.post("/*/promote", this::promotePawn);
        });
    }

    /**
     * Naredi novo igro
     *
     * @param request
     * @param response
     * @return
     * @throws JsonProcessingException
     *
     *
     */
    private Object newGame(Request request, Response response) throws JsonProcessingException {
        Game game = new Game();
        games.add(game);

        GameUpdate gup = new GameUpdate(game.renderFEN(), game.getHistory(), game.getUuid().toString(), game.checkGameStatus(), false);
        ObjectMapper mapper = new ObjectMapper();

        response.header("Content-Type", "application/json");
        return mapper.writeValueAsString(gup);
    }

    /**
     * Handla premike
     *
     * @param request
     * @param response
     * @return
     * @throws JsonProcessingException
     *
     *
     */
    private Object move(Request request, Response response) throws JsonProcessingException {
        UUID uuid = UUID.fromString(request.splat()[0]);
        String body = request.body();
        ObjectMapper mapper = new ObjectMapper();
        MoveInput mi = mapper.readValue(body, MoveInput.class);
        int[] oldC = ChessUtils.convertChessCoordsToInt(mi.source());
        int[] newC = ChessUtils.convertChessCoordsToInt(mi.target());

        NewMove nm = new NewMove(oldC[0], oldC[1], newC[0], newC[1]);

        for (Game game : games) {
            if (game.getUuid().equals(uuid)) {
                GameUpdate gup = game.makeMove(mi.piece().charAt(0) == 'w', nm);
                if (gup.status() == GameStatus.BLACK_CHECKMATE || gup.status() == GameStatus.WHITE_CHECKMATE || gup.status() == GameStatus.STALEMATE) {
                    games.remove(game);
                }

                response.header("Content-Type", "application/json");
                return mapper.writeValueAsString(gup);
            }
        }

        response.status(404);
        return "No game with such UUID";
    }

    /**
     * Vrne tabelo šahovnice
     *
     * @param request
     * @param response
     * @return
     * @throws JsonProcessingException
     *
     *
     */
    private Object getChessboard(Request request, Response response) throws JsonProcessingException {
        UUID uuid = UUID.fromString(request.splat()[0]);

        for (Game game : games) {
            if (game.getUuid().equals(uuid)) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(game.getChessboard());
            }
        }

        response.status(404);
        return "No game with such UUID";
    }

    /**
     * promovira kmeta v izbrano figuro
     *
     * @param request
     * @param response
     * @return
     * @throws JsonProcessingException
     */
    private Object promotePawn(Request request, Response response) throws JsonProcessingException {
        UUID uuid = UUID.fromString(request.splat()[0]);
        String body = request.body();
        ObjectMapper mapper = new ObjectMapper();
        PawnPromotionInput ppi = mapper.readValue(body, PawnPromotionInput.class);


        for (Game game : games) {
            if (game.getUuid().equals(uuid)) {
                if (game.isWhitesTurn() != ppi.isWhite() && game.isCanPawnPromote()) {
                    Figure newf = null;
                    switch (ppi.newFigureFEN()) {
                        case "N" -> newf = new Knight(ppi.x(), ppi.y(), ppi.isWhite());
                        case "B" -> newf = new Bishop(ppi.x(), ppi.y(), ppi.isWhite());
                        case "R" -> newf = new Rook(ppi.x(), ppi.y(), ppi.isWhite());
                        case "Q" -> newf = new Queen(ppi.x(), ppi.y(), ppi.isWhite());
                        default -> {
                            // nekak ti je ratal napačn string dobit skill issue
                            return mapper.writeValueAsString(new GameUpdate(game.renderFEN(), game.getHistory(), uuid.toString(), game.checkGameStatus(), game.isCanPawnPromote()));
                        }
                    }

                    game.getChessboard()[ppi.y()][ppi.x()] = newf;
                }
                game.setCanPawnPromote(false);
                return mapper.writeValueAsString(new GameUpdate(game.renderFEN(), game.getHistory(), uuid.toString(), game.checkGameStatus(), false));
            }
        }

        response.status(404);
        return "No game with such UUID";
    }

    /**
     * metoda za prilagajanje CORS pravil
     *
     * @param origin origin
     * @param methods metode
     * @param headers headerji
     *
     *
     */
    private static void enableCORS(final String origin, final String methods, final String headers) {
        Spark.options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            //response.type("application/json");
        });
    }
}
