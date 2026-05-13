package exception;

public class DataException extends DutyFreeException {

    public DataException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
    
    public DataException(ErrorCode errorCode) {
        super(errorCode);
    }
}