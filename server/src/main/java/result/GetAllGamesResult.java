package result;

import model.GameData;
import java.util.List;

public record GetAllGamesResult(String message, List<GameData> games) {}
