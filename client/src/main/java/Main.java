
import java.util.Scanner;
import ui.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("¡ Welcome to 240 Chess! Type Help to get started. ¿");

        int port = 8080;
        var serverFacade = new ServerFacade(port);

        String authToken = null;
        String username;

        var prelogin = new PreloginUI(serverFacade);
        var postlogin = new PostloginUI(serverFacade);

        while (true) {
            if (authToken == null) {
                System.out.print("[LOGGED_OUT] >>> ");
                var result = prelogin.handle(readLine(scanner));

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
                var result = postlogin.handle(readLine(scanner), authToken);

                switch (result.action()) {
                    case LOGOUT -> {
                        authToken = null;
                        username = null;
                    }
                    case QUIT -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> {}
                }
            }
        }

    }

    public static String[] readLine(Scanner scanner) {
        var line = scanner.nextLine().trim();
        return line.split("\\s+");
    }

}
