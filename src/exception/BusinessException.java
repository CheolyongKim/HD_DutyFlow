package exception;

public class BusinessException extends DutyFreeException {

    public BusinessException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}