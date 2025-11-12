package ui;

import java.io.IOException;
import java.util.Scanner;

public class PreloginUI {
    private final Scanner scanner;
    private final ServerFacade facade;

    public PreloginUI(Scanner scanner, ServerFacade facade) {
        this.scanner = scanner;
        this.facade = facade;
    }

    public PreloginResult handle() {
        System.out.print("[LOGGED_OUT] >>> ");
        var line = scanner.nextLine().trim();
        var parts = line.split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return PreloginResult.invalid();

        var cmd = parts[0].toLowerCase();

        try {
            return switch (cmd) {
                case "help" -> {
                    System.out.println("""
                            register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                            login <USERNAME> <PASSWORD> - to play chess
                            quit - exit the program
                            help - list possible commands
                            """);
                    yield PreloginResult.help();
                }
                case "quit" -> PreloginResult.quit();
                case "register" -> {
                    if (parts.length != 4) {
                        System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                        yield PreloginResult.invalid();
                    }
                    var auth = facade.register(parts[1], parts[2], parts[3]);
                    yield PreloginResult.success(auth);
                }
                case "login" -> {
                    if (parts.length != 3) {
                        System.out.println("Usage: login <USERNAME> <PASSWORD>");
                        yield PreloginResult.invalid();
                    }
                    var auth = facade.login(parts[1], parts[2]);
                    yield PreloginResult.success(auth);
                }
                default -> {
                    System.out.println("Unknown command. Type 'help' for options.");
                    yield PreloginResult.invalid();
                }
            };
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            return PreloginResult.invalid();
        }
    }

}
