package exception;

public class ValidationException extends DutyFreeException{
	
	public ValidationException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
	
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
