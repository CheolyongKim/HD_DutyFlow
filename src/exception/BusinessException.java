package exception;

public class BusinessException extends DutyFreeException {

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}