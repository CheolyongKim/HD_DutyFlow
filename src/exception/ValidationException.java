package exception;

public class ValidationException extends DutyFreeException{
	
	public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }
	
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
