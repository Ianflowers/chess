package ui;

import model.GameData;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PostloginUI {
    private final ServerFacade facade;

    public PostloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public Result handle(String[] parts, String authToken) {
        try {
            return switch (parts[0].toLowerCase()) {
                case "help" -> handleHelp();
                case "logout" -> handleLogout(authToken);
                case "quit" -> Result.quit();
                case "create" -> handleCreate(parts, authToken);
                case "list" -> handleList(authToken);
                case "join" -> handleJoin(parts, authToken);
                case "observe" -> handleObserve(parts, authToken);
                default -> {
                    System.out.println("Unknown command. Type 'help' for options.");
                    yield Result.invalid();
                }
            };
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return Result.invalid();
        }
    }

    private Result handleHelp() {
        System.out.println("""
                create <NAME> - create a game
                list - list games
                join <NUMBER> [WHITE|BLACK] - join a game
                observe <NUMBER> - observe a game
                logout - log out
                quit - exit the program
                help - list possible commands
                """);
        return Result.none();
    }

    private Result handleLogout(String authToken) throws IOException {
        facade.logout(authToken);
        System.out.println("Logged out.");
        return Result.logout();
    }

    private Result handleCreate(String[] parts, String authToken) throws IOException {
        if (parts.length != 2) {
            System.out.println("Usage: create <NAME>");
            return Result.invalid();
        }
        facade.createGame(parts[1], authToken);
        System.out.println("Game created: " + parts[1]);
        return Result.none();
    }

    private Result handleList(String authToken) throws IOException {
        List<GameData> games = facade.listGames(authToken);
        if (games.isEmpty()) {
            System.out.println("No games available.");
        } else {
            int i = 1;
            for (var g : games) {
                System.out.printf("%d. %s (White: %s, Black: %s)%n",
                        i++,
                        g.gameName(),
                        Optional.ofNullable(g.whiteUsername()).orElse("-"),
                        Optional.ofNullable(g.blackUsername()).orElse("-"));
            }
        }
        return Result.none();
    }

    private Result handleJoin(String[] parts, String authToken) throws IOException {
        if (parts.length != 3) {
            System.out.println("Usage: join <NUMBER> [WHITE|BLACK]");
            return Result.invalid();
        }

        GameData game = getGameFromList(parts[1], authToken);
        if (game == null) {
            return Result.invalid();
        }

        facade.joinGame(game.gameID(), parts[2].toUpperCase(), authToken);
        System.out.println("Joined game: " + game.gameName());
        BoardDrawer.drawBoard(parts[2].equalsIgnoreCase("white"), game.game());
        return Result.none();
    }

    private Result handleObserve(String[] parts, String authToken) throws IOException {
        if (parts.length != 2) {
            System.out.println("Usage: observe <NUMBER>");
            return Result.invalid();
        }

        GameData game = getGameFromList(parts[1], authToken);
        if (game == null) {
            return Result.invalid();
        }

        System.out.println("Observing game: " + game.gameName());
        BoardDrawer.drawBoard(true, game.game());
        return Result.none();
    }

    private GameData getGameFromList(String gameNumberStr, String authToken) throws IOException {
        int index;
        try {
            index = Integer.parseInt(gameNumberStr) - 1;
        } catch (NumberFormatException ex) {
            System.out.println("Invalid game number format.");
            return null;
        }

        List<GameData> games = facade.listGames(authToken);
        if (index < 0 || index >= games.size()) {
            System.out.println("Invalid game number.");
            return null;
        }

        return games.get(index);
    }

}
