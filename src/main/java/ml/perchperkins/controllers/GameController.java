package ml.perchperkins.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.io.GameUpdate;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    @Getter
    private List<Game> games = new ArrayList<>();
    public GameController() {
        Spark.path("/g", ()->{
           Spark.get("/new", this::newGame);
           Spark.post("/*/move", this::move);
        });
    }

    private Object newGame(Request request, Response response) throws JsonProcessingException {
        Game game = new Game();
        games.add(game);

        GameUpdate gup = new GameUpdate(game.renderFEN(), game.getHistory());
        ObjectMapper mapper = new ObjectMapper();

        response.header("Content-Type", "application/json");
        return mapper.writeValueAsString(gup);
    }

    private Object move(Request request, Response response) {
        return "";
    }
}
