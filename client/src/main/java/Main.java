
import chess.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("¡ Welcome to 240 Chess! Type Help to get started. ¿");

        boolean quit = false;

        while (!quit) {
            System.out.print("[LOGGED_OUT] >>> ");
            var line = scanner.nextLine().trim();
            var parts = line.split("\\s+");
            var cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "help":
                    System.out.println("""
                                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                                login <USERNAME> <PASSWORD> - to play chess
                                quit - exit the program
                                help - list possible commands
                                """);
                    break;

                case "quit":
                    quit = true;
                    break;

                case "register":
                    if (parts.length != 4) {
                        System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                        break;
                    }
                    System.out.println("Registering account...");
                    break;

                case "login":
                    if (parts.length != 3) {
                        System.out.println("Usage: login <USERNAME> <PASSWORD>");
                        break;
                    }
                    System.out.println("Searching for account...");
                    break;

                default:
                    System.out.println("Unknown command. Type 'help' for options.");
                    break;

            }

        }

        System.out.println("Goodbye!");
    }
}
