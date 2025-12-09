
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
                case LOGGED_OUT -> {
                    Result result = prelogin.handle(parts);
                    switch (result.action()) {
                        case LOGIN_SUCCESS -> {
                            auth = result.authData();
                            System.out.println("Logged in as " + auth.username());
                            state = State.LOGGED_IN;
                        }
                        case QUIT -> {
                            state = State.QUIT;
                        }
                        default -> {
                        }
                    }
                }
                case LOGGED_IN -> {
                    Result result = postlogin.handle(parts, auth.authToken());
                    switch (result.action()) {
                        case JOIN -> {
                            gameplay = postlogin.joinGame(parts, auth.authToken());
                            game = postlogin.getGameFromList(parts[1], auth.authToken());
                            if (parts.length == 3) {
                                state = State.PLAYING;
                            } else {
                                state = State.OBSERVING;
                            }
                        }
                        case LOGOUT -> {
                            state = State.LOGGED_OUT;
                            auth = null;
                        }
                        case QUIT -> {
                            state = State.QUIT;
                            auth = null;
                        }
                        default -> {
                        }
                    }
                }
                case PLAYING -> {
                    Result result = gameplay.handlePlayer(parts, auth.authToken(), game.gameID());

                    switch (result.action()) {
                        case PROMOTE -> {
                            boolean select = true;
                            while (select) {
                                System.out.print("Promote pawn to (Q, R, B, N): ");
                                String[] choice = readLine(scanner);
                                select = gameplay.handlePromotion(choice[0]);
                            }
                        }
                        case MOVE -> {
                            gameplay.finishMove(auth.authToken(), game.gameID());
                        }
                        case QUIT -> {
                            state = State.LOGGED_IN;
                            gameplay = null;
                            game = null;
                        }
                    }
                }
                case OBSERVING -> {
                    Result result = gameplay.handleObserver(parts, auth.authToken(), game.gameID());

                    switch (result.action()) {
                        case QUIT -> {
                            state = State.LOGGED_IN;
                            gameplay = null;
                            game = null;
                        }
                    }
                }

            }
        }
        System.out.println("Goodbye!");

    }

    private static String promptFor(State state) {
        return "[" + state + "] >>> ";
    }

    public static String[] readLine(Scanner scanner) {
        var line = scanner.nextLine().trim().toLowerCase();
        return line.split("\\s+");
    }


}
