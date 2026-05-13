package exception;

public class DataNotFoundException extends DutyFreeException {

    public DataNotFoundException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
    
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
