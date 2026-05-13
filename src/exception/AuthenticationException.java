package exception;

public class AuthenticationException extends DutyFreeException {

    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}