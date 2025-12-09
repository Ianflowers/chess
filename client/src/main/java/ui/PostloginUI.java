package ui;

import model.GameData;
import websocket.WebSocketClient;
import websocket.WebSocketListener;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PostloginUI {
    private final ServerFacade facade;

    public PostloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public Result handle(String[] parts, String authToken) throws IOException {
        return switch(parts[0]) {
            case "help" -> handleHelp();
            case "logout" -> handleLogout(authToken);
            case "create" -> handleCreate(parts, authToken);
            case "list" -> handleList(authToken);
            case "join" -> handleJoin(parts, authToken);
            case "observe" -> handleObserve(parts, authToken);
            case "quit" -> Result.quit();
            default -> {
                System.out.println("Unknown command. Type 'help' for options.\n");
                yield Result.invalid();
            }
        };
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
            System.out.println("Usage: create <NAME>\n");
            return Result.invalid();
        }
        facade.createGame(parts[1], authToken);
        System.out.println("Game created: " + parts[1] + "\n");
        return Result.none();
    }

    private Result handleList(String authToken) throws IOException {
        List<GameData> games = facade.listGames(authToken);
        if (games.isEmpty()) {
            System.out.println("No games available.\n");
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
        System.out.println();
        return Result.none();
    }

    private Result handleJoin(String[] parts, String authToken) throws IOException {
        if (parts.length != 3) {
            System.out.println("Usage: join <NUMBER> [WHITE|BLACK]\n");
            return Result.invalid();
        }

        GameData game = getGameFromList(parts[1], authToken);
        if (game == null) {
            return Result.invalid();
        }

        facade.joinGame(game.gameID(), parts[2], authToken);
        return Result.join();
    }

    private Result handleObserve(String[] parts, String authToken) throws IOException {
        if (parts.length != 2) {
            System.out.println("Usage: observe <NUMBER>\n");
            return Result.invalid();
        }

        GameData game = getGameFromList(parts[1], authToken);
        if (game == null) {
            return Result.invalid();
        }

        System.out.println("Observing game: " + game.gameName() + "\n");
        return Result.join();
    }

    public GameplayUI joinGame(String[] parts, String authToken) throws IOException {
        GameData game = getGameFromList(parts[1], authToken);

        boolean whitePerspective = true;
        if (parts.length == 3) {
            whitePerspective = parts[2].equalsIgnoreCase("white");
        }

        GameplayUI ui = new GameplayUI(whitePerspective, null);
        WebSocketListener listener = new WebSocketListener(ui);
        WebSocketClient client = new WebSocketClient(listener);
        client.connect("ws://localhost:8080/ws")
                .thenAccept(ws -> {
                    ui.setWebSocket(ws);

                    client.send(new UserGameCommand(
                            UserGameCommand.CommandType.CONNECT,
                            authToken,
                            game.gameID()
                    ));
                });

        return ui;
    }

    public GameData getGameFromList(String gameNumberStr, String authToken) throws IOException {
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
