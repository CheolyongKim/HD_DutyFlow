package exception;

public class DataNotFoundException extends DutyFreeException {

    public DataNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
