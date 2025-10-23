package dataaccess;

public class UnauthorizedException extends DataAccessException {
    public UnauthorizedException() { super("Error: unauthorized"); }
    public UnauthorizedException(String message) { super(message); }
    public UnauthorizedException(String message, Throwable cause) { super(message, cause); }

}
