package exception;

public class ValidationException extends DutyFreeException{
	
	public ValidationException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
	
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
