package request;

public record UpdateGameRequest(
        String gameId,
        String whiteUsername,
        String blackUsername,
        String gameName) {
}
