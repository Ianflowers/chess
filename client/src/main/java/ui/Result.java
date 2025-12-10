package ui;

import model.AuthData;

public record Result(Action action, AuthData authData) {

    public enum Action { HELP, INVALID, QUIT, LOGIN_SUCCESS, JOIN, PROMOTE, MOVE, RESIGN, LOGOUT, NONE }

    public static Result help() {
        return new Result(Action.HELP, null);
    }
    public static Result invalid() {
        return new Result(Action.INVALID, null);
    }
    public static Result quit() {
        return new Result(Action.QUIT, null);
    }
    public static Result success(AuthData auth) { return new Result(Action.LOGIN_SUCCESS, auth); }
    public static Result join() { return new Result(Action.JOIN, null); }
    public static Result move() {
        return new Result(Action.MOVE, null);
    }
    public static Result resign() {
        return new Result(Action.RESIGN, null);
    }

    public static Result promote() {
        return new Result(Action.PROMOTE, null);
    }
    public static Result logout() {
        return new Result(Action.LOGOUT, null);
    }
    public static Result none() {
        return new Result(Action.NONE, null);
    }
}
