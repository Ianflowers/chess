package result;

import model.GameData;

public record GetGameResult(String message, GameData game) {
    public GetGameResult(String message) { this(message, null); }

}
