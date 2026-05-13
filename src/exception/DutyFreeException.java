package exception;

public class DutyFreeException extends RuntimeException {

    private final String errorCode;

    public DutyFreeException(String errorCode, String logMessage) {
        super(logMessage);
        this.errorCode = errorCode;
    }
    
    public DutyFreeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }

    public String getErrorCode() {
        return errorCode;
    }
}