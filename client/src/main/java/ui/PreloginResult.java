package ui;

import model.AuthData;

public record PreloginResult(Action action, AuthData authData) {
    public enum Action { HELP, INVALID, QUIT, LOGIN_SUCCESS }

    public static PreloginResult help() {
        return new PreloginResult(Action.HELP, null);
    }

    public static PreloginResult invalid() {
        return new PreloginResult(Action.INVALID, null);
    }

    public static PreloginResult quit() {
        return new PreloginResult(Action.QUIT, null);
    }

    public static PreloginResult success(AuthData auth) {
        return new PreloginResult(Action.LOGIN_SUCCESS, auth);
    }
}
