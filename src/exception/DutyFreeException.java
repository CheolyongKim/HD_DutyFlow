package exception;

public class DutyFreeException extends RuntimeException {

    private final String errorCode;

    public DutyFreeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}