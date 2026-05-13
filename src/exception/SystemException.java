package exception;

public class SystemException extends DutyFreeException {

    public SystemException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
}