package exception;

public class DutyFreeException extends RuntimeException {

    private final ErrorCode errorCode;
    private static final SystemLogDAO systemLogDAO = new SystemLogDAO();

    public DutyFreeException(ErrorCode errorCode, Exception message) {
        super(message);
        this.errorCode = errorCode;
        systemLogDAO.save( errorCode.getCode(),
                		   message != null ? message.getMessage() : errorCode.getMessage()
            );
    }
    
    public DutyFreeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        systemLogDAO.save( errorCode.getCode(),
                		   errorCode.getMessage()  
            );
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
}