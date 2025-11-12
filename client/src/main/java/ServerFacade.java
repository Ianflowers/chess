
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws IOException {
        var body = Map.of("username", username, "password", password, "email", email);
        return makeRequest("POST", "/user", body, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws IOException {
        var body = Map.of("username", username, "password", password);
        return makeRequest("POST", "/session", body, null, AuthData.class);
    }

    public void logout(String authToken) throws IOException {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public void createGame(String gameName, String authToken) throws IOException {
        var body = Map.of("gameName", gameName);
        makeRequest("POST", "/game", body, authToken, null);
    }

    public List<GameData> listGames(String authToken) throws IOException {
        var result = makeRequest("GET", "/game", null, authToken, GameListResult.class);
        return result.games;
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws IOException {
        var body = Map.of("gameID", gameID, "playerColor", playerColor);
        makeRequest("PUT", "/game", body, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object body, String authToken, Class<T> responseClass) throws IOException {
        URL url = new URL(serverUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        if (authToken != null) {
            conn.setRequestProperty("Authorization", authToken);
        }

        if (body != null) {
            try (var out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(gson.toJson(body));
            }
        }

        int status = conn.getResponseCode();
        Reader reader = new InputStreamReader(
                (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream()
        );

        if (status >= 200 && status < 300) {
            if (responseClass == null) return null;
            return gson.fromJson(reader, responseClass);
        } else {
            Map<String, String> error = gson.fromJson(reader, Map.class);
            String message = (error != null) ? error.getOrDefault("message", "Unknown error") : "Server error";
            throw new IOException(message);
        }
    }

    private static class GameListResult {
        public List<GameData> games;
    }

}
