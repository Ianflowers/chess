package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public class WebSocketClient {

    private final Gson gson = new Gson();
    private WebSocket socket;
    private final WebSocketListener listener;

    public WebSocketClient(WebSocketListener listener) {
        this.listener = listener;
    }

    public CompletableFuture<WebSocket> connect(String url) {
        return HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(url), listener)
                .thenApply(ws -> {
                    this.socket = ws;
                    return ws;
                });
    }

    public WebSocket getSocket() { return socket; }

    public void send(UserGameCommand cmd) {
        if (socket != null) {
            socket.sendText(gson.toJson(cmd), true);
        }
    }

    public void close() {
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
            socket = null;
        }
    }

}
