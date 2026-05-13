package exception;

public class SystemException extends DutyFreeException {

    public SystemException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
}