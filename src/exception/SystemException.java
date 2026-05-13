package exception;

public class SystemException extends DutyFreeException {

    public SystemException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
}