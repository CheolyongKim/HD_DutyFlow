package exception;

public class QueueException extends DutyFreeException {

    public QueueException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public QueueException(ErrorCode errorCode) {
        super(errorCode);
    }
}