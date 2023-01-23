package ml.perchperkins.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ml.perchperkins.controllers.GameController;
import ml.perchperkins.objects.Game;
import ml.perchperkins.objects.UserSession;
import ml.perchperkins.objects.io.GameUpdate;
import ml.perchperkins.objects.io.MoveInput;
import ml.perchperkins.objects.io.NewMove;
import ml.perchperkins.utils.ChessUtils;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class WebSocketHandler {

    @OnWebSocketConnect
    public void connected(Session session) {
        UUID uuid = UUID.fromString(session.getUpgradeRequest().getParameterMap().get("uuid").get(0)); // ?uuid=jfadsč
        boolean isWhite = Boolean.getBoolean(session.getUpgradeRequest().getParameterMap().get("iswhite").get(0)); // &iswhite=true
        if (isWhite) {
            GameController.games.get(uuid).players.get("white").user = session;
        } else {
            GameController.games.get(uuid).players.get("black").user = session;
        }

        if (GameController.games.get(uuid).players.get("white").user != null && GameController.games.get(uuid).players.get("black").user != null) {
            GameController.games.get(uuid).broadcastGameInfo();
        }
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Got: " + message);   // Print message
        UUID uuid = UUID.fromString(session.getUpgradeRequest().getParameterMap().get("uuid").get(0));

        ObjectMapper mapper = new ObjectMapper();
        MoveInput mi = mapper.readValue(message, MoveInput.class);
        int[] oldC = ChessUtils.convertChessCoordsToInt(mi.source());
        int[] newC = ChessUtils.convertChessCoordsToInt(mi.target());

        NewMove nm = new NewMove(oldC[0], oldC[1], newC[0], newC[1]);

        Game game = GameController.games.get(uuid);
        if (game != null) {
            GameUpdate gup = game.makeMove(mi.piece().charAt(0) == 'w', nm);

            game.broadcastGameInfo();
        }
    }
}