
import java.io.IOException;
import java.util.Scanner;

import model.AuthData;
import model.GameData;
import ui.*;

public class Main {

    enum State { LOGGED_OUT, LOGGED_IN, PLAYING, OBSERVING, QUIT }
    static State state = State.LOGGED_OUT;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("¡ Welcome to 240 Chess! Type Help to get started. ¿");

        int port = 8080;
        var serverFacade = new ServerFacade(port);

        PreloginUI prelogin = new PreloginUI(serverFacade);
        PostloginUI postlogin = new PostloginUI(serverFacade);

        GameplayUI gameplay = null;
        GameData game = null;
        AuthData auth = null;

        while (state != State.QUIT) {
            System.out.print(promptFor(state));
            String[] parts = readLine(scanner);

            switch (state) {
                case LOGGED_OUT ->
                        auth = handleLoggedOut(parts, prelogin);

                case LOGGED_IN -> {
                    var result = handleLoggedIn(parts, postlogin, auth);
                    if (result instanceof LoginContext ctx) {
                        gameplay = ctx.gameplay();
                        game = ctx.game();
                    }
                }

                case PLAYING ->
                        handlePlaying(parts, scanner, gameplay, auth, game);

                case OBSERVING ->
                        handleObserving(parts, gameplay, auth, game);

                default -> { }
            }
        }
        System.out.println("Goodbye!");
    }

    private static AuthData handleLoggedOut(String[] parts, PreloginUI ui) {
        Result result = ui.handle(parts);

        return switch (result.action()) {
            case LOGIN_SUCCESS -> {
                AuthData auth = result.authData();
                System.out.println("Logged in as " + auth.username() + "\n");
                state = State.LOGGED_IN;
                yield auth;
            }
            case QUIT -> {
                state = State.QUIT;
                yield null;
            }
            default -> null;
        };
    }

    private record LoginContext(GameplayUI gameplay, GameData game) {}

    private static LoginContext handleLoggedIn(String[] parts, PostloginUI ui, AuthData auth) throws IOException {
        Result result = ui.handle(parts, auth.authToken());

        return switch (result.action()) {
            case JOIN -> {
                GameplayUI gameplay = ui.joinGame(parts, auth.authToken());
                GameData game = ui.getGameFromList(parts[1], auth.authToken());
                state = (parts.length == 3) ? State.PLAYING : State.OBSERVING;
                yield new LoginContext(gameplay, game);
            }
            case LOGOUT -> {
                state = State.LOGGED_OUT;
                yield null;
            }
            case QUIT -> {
                state = State.QUIT;
                yield null;
            }
            default -> null;
        };
    }

    private static void handlePlaying(String[] parts, Scanner scanner,
                                      GameplayUI gameplay, AuthData auth, GameData game) {
        Result result = gameplay.handlePlayer(parts, auth.authToken(), game.gameID());

        switch (result.action()) {
            case PROMOTE -> {
                doPromotion(scanner, gameplay);
            }

            case MOVE -> {
                gameplay.finishMove(auth.authToken(), game.gameID());
            }

            case RESIGN ->
            {
                if (confirmResign(scanner)) {
                    gameplay.sendResign(auth.authToken(), game.gameID());
                }
            }

            case QUIT -> {
                state = State.LOGGED_IN;
            }
            default -> { }
        }
    }

    private static void handleObserving(String[] parts, GameplayUI gameplay,
                                        AuthData auth, GameData game) {
        Result result = gameplay.handleObserver(parts, auth.authToken(), game.gameID());

        if (result.action() == Result.Action.QUIT) {
            state = State.LOGGED_IN;
        }
    }

    private static void doPromotion(Scanner scanner, GameplayUI gameplay) {
        boolean selecting = true;
        while (selecting) {
            System.out.print("Promote pawn to (Q, R, B, N): ");
            selecting = gameplay.handlePromotion(readLine(scanner)[0]);
        }
    }

    private static boolean confirmResign(Scanner scanner) {
        System.out.print("Are you sure you wish to resign? [Y/N]");
        while (true) {
            String response = readLine(scanner)[0];

            switch (response.toLowerCase()) {
                case "yes", "y" -> {
                    return true;
                }

                case "no", "n" -> {
                    return false;
                }
                default -> {
                    System.out.print("\nInvalid Response. Are you sure you wish to resign? [Y/N]");
                }
            }
        }
    }

    private static String promptFor(State state) {
        return "[" + state + "] >>> ";
    }

    public static String[] readLine(Scanner scanner) {
        return scanner.nextLine().trim().toLowerCase().split("\\s+");
    }

}
