package result;

import model.GameData;

public record CreateGameResult( String message, GameData game) {
    public CreateGameResult(String message) { this(message, null); }

}
