package ml.perchperkins.handlers;

import ml.perchperkins.controllers.GameController;
import ml.perchperkins.objects.UserSession;
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
        boolean isWhite = Boolean.getBoolean(session.getUpgradeRequest().getParameterMap().get("isWhite").get(0));
        GameController.userUUIDmap.put(session, new UserSession(uuid, isWhite));
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        GameController.userUUIDmap.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Got: " + message);   // Print message

    }
}