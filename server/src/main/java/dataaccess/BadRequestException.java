package dataaccess;

public class BadRequestException extends DataAccessException {
    public BadRequestException() { super("Error: bad request"); }
    public BadRequestException(String message) { super(message); }
    public BadRequestException(String message, Throwable cause) { super(message, cause); }

}
