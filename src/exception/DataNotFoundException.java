package exception;

public class DataNotFoundException extends DutyFreeException {

    public DataNotFoundException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
    
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
