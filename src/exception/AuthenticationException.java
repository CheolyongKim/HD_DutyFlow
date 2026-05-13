package exception;

public class AuthenticationException extends DutyFreeException {

    public AuthenticationException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}