package dataaccess;

public class ForbiddenException extends DataAccessException {
    public ForbiddenException() { super("Error: already taken"); }
    public ForbiddenException(String message) { super(message); }
    public ForbiddenException(String message, Throwable cause) { super(message, cause); }

}
