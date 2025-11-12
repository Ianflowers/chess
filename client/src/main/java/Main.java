
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
                System.out.println("Welcome " + username + "!");
            }
        }
    }
}
