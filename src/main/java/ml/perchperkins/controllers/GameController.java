package ml.perchperkins.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import ml.perchperkins.handlers.WebSocketHandler;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.UserSession;
import ml.perchperkins.objects.io.GameUpdate;
import ml.perchperkins.objects.io.MoveInput;
import ml.perchperkins.objects.io.NewGame;
import ml.perchperkins.objects.io.NewMove;
import ml.perchperkins.utils.ChessUtils;
import org.eclipse.jetty.websocket.api.Session;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameController {

    public static Map<UUID, Game> games = new ConcurrentHashMap<>();
//    private static List<Game> games = new ArrayList<>();
    public GameController() {
        Spark.webSocket("/game", WebSocketHandler.class); // apparently to nardi singleton???
        enableCORS("*", "*", "*");
        Spark.path("/g", ()->{
           Spark.get("/new", this::newGame);
           Spark.get("/*/join", this::joinGame);
           Spark.post("/*/move", this::move);
           Spark.get("/*/chessboard", this::getChessboard);
        });
    }

    private Object newGame(Request request, Response response) throws JsonProcessingException {
        Game game = new Game();
        games.put(game.getUuid(), game);

        NewGame gup = new NewGame(game.renderFEN(), game.getHistory(), game.getUuid().toString(), game.checkGameStatus(), true);
        ObjectMapper mapper = new ObjectMapper();

        response.header("Content-Type", "application/json");
        return mapper.writeValueAsString(gup);
    }

    private Object move(Request request, Response response) throws JsonProcessingException {
        UUID uuid = UUID.fromString(request.splat()[0]);
        String body = request.body();
        ObjectMapper mapper = new ObjectMapper();
        MoveInput mi = mapper.readValue(body, MoveInput.class);
        int[] oldC = ChessUtils.convertChessCoordsToInt(mi.source());
        int[] newC = ChessUtils.convertChessCoordsToInt(mi.target());

        NewMove nm = new NewMove(oldC[0], oldC[1], newC[0], newC[1]);

        Game game = games.get(uuid);
        if (game != null) {
            GameUpdate gup = game.makeMove(mi.piece().charAt(0) == 'w', nm);

            response.header("Content-Type", "application/json");
            return mapper.writeValueAsString(gup);
        }

        response.status(404);
        return "No game with such UUID";
    }

    private Object joinGame(Request request, Response response) throws JsonProcessingException {
        UUID uuid = UUID.fromString(request.splat()[0]);
        Game game = games.get(uuid);

        NewGame gup = new NewGame(game.renderFEN(), game.getHistory(), game.getUuid().toString(), game.checkGameStatus(), false);
        ObjectMapper mapper = new ObjectMapper();

        response.header("Content-Type", "application/json");
        return mapper.writeValueAsString(gup);
    }

    private Object getChessboard(Request request, Response response) throws JsonProcessingException {
        UUID uuid = UUID.fromString(request.splat()[0]);

        Game game = games.get(uuid);
        if (game != null) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(game.renderChessBoard());
        }

        response.status(404);
        return "No game with such UUID";
    }

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
