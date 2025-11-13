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
                case "help" -> {
                    System.out.println("""
                            create <NAME> - create a game
                            list - list games
                            join <NUMBER> [WHITE|BLACK] - join a game
                            observe <NUMBER> - observe a game
                            logout - log out
                            quit - exit the program
                            help - list possible commands
                            """);
                    yield Result.none();
                }

                case "logout" -> {
                    facade.logout(authToken);
                    System.out.println("Logged out.");
                    yield Result.logout();
                }

                case "quit" -> {
                    yield Result.quit();
                }

                case "create" -> {
                    if (parts.length != 2) {
                        System.out.println("Usage: create <NAME>");
                        yield Result.invalid();
                    }
                    facade.createGame(parts[1], authToken);
                    System.out.println("Game created: " + parts[1]);
                    yield Result.none();
                }

                case "list" -> {
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
                    yield Result.none();
                }

                case "join" -> {
                    if (parts.length != 3) {
                        System.out.println("Usage: join <NUMBER> [WHITE|BLACK]");
                        yield Result.invalid();
                    }

                    int index;
                    try {
                        index = Integer.parseInt(parts[1]) - 1;
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid game number format.");
                        yield Result.invalid();
                    }

                    List<GameData> games = facade.listGames(authToken);
                    if (index < 0 || index >= games.size()) {
                        System.out.println("Invalid game number.");
                        yield Result.invalid();
                    }

                    GameData game = games.get(index);
                    facade.joinGame(game.gameID(), parts[2].toUpperCase(), authToken);
                    System.out.println("Joined game: " + game.gameName());

                    BoardDrawer.drawBoard(parts[2].equalsIgnoreCase("white"));
                    yield Result.none();
                }

                case "observe" -> {
                    if (parts.length != 2) {
                        System.out.println("Usage: observe <NUMBER>");
                        yield Result.invalid();
                    }

                    int index;
                    try {
                        index = Integer.parseInt(parts[1]) - 1;
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid game number format.");
                        yield Result.invalid();
                    }

                    List<GameData> games = facade.listGames(authToken);
                    if (index < 0 || index >= games.size()) {
                        System.out.println("Invalid game number.");
                        yield Result.invalid();
                    }

                    GameData game = games.get(index);
                    System.out.println("Observing game: " + game.gameName());

                    BoardDrawer.drawBoard(true);
                    yield Result.none();
                }

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
}
