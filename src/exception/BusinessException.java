package exception;

public class BusinessException extends DutyFreeException {

    public BusinessException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}