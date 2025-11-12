
import java.util.Scanner;
import ui.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("¡ Welcome to 240 Chess! Type Help to get started. ¿");

        int port = 8080;
        var serverFacade = new ServerFacade(port);

        String authToken = null;
        String username = null;

        var prelogin = new PreloginUI(scanner, serverFacade);

        while (true) {
            if (authToken == null) {
                var result = prelogin.handle();

                switch (result.action()) {
                    case QUIT -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    case LOGIN_SUCCESS -> {
                        var auth = result.authData();
                        authToken = auth.authToken();
                        username = auth.username();
                        System.out.println("Logged in as " + username);
                    }
                    default -> {}
                }
            } else {
                System.out.print("[LOGGED_IN] >>> ");
                var line = scanner.nextLine().trim();
                var parts = line.split("\\s+");
                var cmd = parts[0].toLowerCase();

                switch (cmd) {
                    case "help":
                        System.out.println("""
                            create <NAME> - create a game
                            list - list games
                            join <NUMBER> [WHITE|BLACK] - join a game
                            observe <NUMBER> - observe a game
                            logout - when you are done
                            quit - exit the program
                            help - list possible commands
                            """);
                        break;

                    case "quit":
                        return;

                    case "create":
                        if (parts.length != 2) {
                            System.out.println("Usage: create <NAME>");
                            break;
                        }
                        System.out.println("Creating game...");
                        break;

                    case "list":
                        System.out.println("Displaying games...");
                        break;

                    case "join":
                        if (parts.length != 3) {
                            System.out.println("Usage: join <NUMBER> [WHITE|BLACK]");
                            break;
                        }
                        System.out.println("Joining game...");
                        break;

                    case "observe":
                        if (parts.length != 2) {
                            System.out.println("Usage: observe <NUMBER>");
                            break;
                        }
                        System.out.println("Observing game...");
                        break;

                    case "logout":
                        System.out.println("Logging out...");
                        authToken = null;
                        break;

                    default:
                        System.out.println("Unknown command. Type 'help' for options.");
                        break;

                }
            }
        }

    }
}
