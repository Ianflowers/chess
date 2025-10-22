package result;

import model.GameData;

public record UpdateGameResult(String message, GameData game) {
    public UpdateGameResult(String message) { this(message, null); }

}
