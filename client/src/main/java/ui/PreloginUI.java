package ui;

import java.io.IOException;

public class PreloginUI {
    private final ServerFacade facade;

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public Result handle(String[] parts) {
        try {
            return switch (parts[0].toLowerCase()) {
                case "help" -> {
                    System.out.println("""
                            register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                            login <USERNAME> <PASSWORD> - to play chess
                            quit - exit the program
                            help - list possible commands
                            """);
                    yield Result.help();
                }

                case "quit" -> {
                    yield Result.quit();
                }

                case "register" -> {
                    if (parts.length != 4) {
                        System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>\n");
                        yield Result.invalid();
                    }
                    var auth = facade.register(parts[1], parts[2], parts[3]);
                    yield Result.success(auth);
                }

                case "login" -> {
                    if (parts.length != 3) {
                        System.out.println("Usage: login <USERNAME> <PASSWORD>\n");
                        yield Result.invalid();
                    }
                    var auth = facade.login(parts[1], parts[2]);
                    yield Result.success(auth);
                }

                default -> {
                    System.out.println("Unknown command. Type 'help' for options.\n");
                    yield Result.invalid();
                }
            };
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return Result.invalid();
        }
    }

}
