package ui;

import model.AuthData;

public record Result(Action action, AuthData authData) {

    public enum Action {
        HELP,          // Prelogin: help menu
        INVALID,       // Prelogin/Postlogin: invalid command
        QUIT,          // Prelogin/Postlogin: quit program
        LOGIN_SUCCESS, // Prelogin: successful login
        LOGOUT,        // Postlogin: logout
        NONE           // Postlogin: valid command that does not change login state
    }


    public static Result help() { return new Result(Action.HELP, null); }
    public static Result invalid() { return new Result(Action.INVALID, null); }
    public static Result quit() { return new Result(Action.QUIT, null); }
    public static Result success(AuthData auth) { return new Result(Action.LOGIN_SUCCESS, auth); }
    public static Result logout() { return new Result(Action.LOGOUT, null); }
    public static Result none() { return new Result(Action.NONE, null); }
}
