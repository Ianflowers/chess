package result;

import model.UserData;

public record UserResult(String message, UserData user) {
    public UserResult(String message) { this(message, null); }

}
