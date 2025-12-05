package websocket;

import com.google.gson.Gson;
import ui.GameplayUI;
import websocket.messages.*;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketListener implements WebSocket.Listener {

    private final Gson gson = new Gson();
    private final GameplayUI ui;

    public WebSocketListener(GameplayUI ui) { this.ui = ui; }

    @Override
    public void onOpen(WebSocket webSocket) { webSocket.request(1); }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String json = data.toString();
        ServerMessage base = gson.fromJson(json, ServerMessage.class);

        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                ui.redrawBoard(msg.getGame().game());
            }

            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                ui.showNotification(msg.getMessage());
            }

            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                ui.showError(msg.getErrorMessage());
            }
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        ui.onDisconnect();
        return null;
    }

}
