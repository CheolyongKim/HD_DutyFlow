package exception;

public class DataException extends DutyFreeException {

    public DataException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public DataException(ErrorCode errorCode) {
        super(errorCode);
    }
}