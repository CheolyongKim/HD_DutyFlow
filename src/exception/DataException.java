package exception;

public class DataException extends DutyFreeException {

    public DataException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
    
    public DataException(ErrorCode errorCode) {
        super(errorCode);
    }
}