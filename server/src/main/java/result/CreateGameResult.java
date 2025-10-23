package result;

import model.GameData;

public record CreateGameResult(String message, GameData game) {
    public Integer getGameID() { return game == null ? null : game.gameID(); }

}
