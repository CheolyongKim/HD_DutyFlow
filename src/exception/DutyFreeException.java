package exception;

public class DutyFreeException extends RuntimeException {

    private final ErrorCode errorCode;

    public DutyFreeException(ErrorCode errorCode, Exception e) {
        super(e);
        this.errorCode = errorCode;
    }
    
    public DutyFreeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
}