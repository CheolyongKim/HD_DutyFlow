package exception;

public class AuthenticationException extends DutyFreeException {

    public AuthenticationException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}