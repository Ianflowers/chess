package request;

public record CreateGameRequest(
        String whiteUsername,
        String blackUsername,
        String gameName,
        String gameId) {
}
