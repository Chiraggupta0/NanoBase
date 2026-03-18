package exception;

public class DBException extends RuntimeException {

    private final ErrorType errorType;

    public enum ErrorType {
        SYNTAX_ERROR,
        TABLE_NOT_FOUND,
        TABLE_ALREADY_EXISTS,
        COLUMN_NOT_FOUND,
        INVALID_VALUE,
        STORAGE_ERROR,
        DUPLICATE_KEY
    }

    public DBException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return "[" + errorType + "] " + getMessage();
    }
}