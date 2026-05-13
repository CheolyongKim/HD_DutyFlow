package exception;

public class QueueException extends DutyFreeException {

    public QueueException(ErrorCode errorCode, Exception e) {
        super(errorCode, e);
    }
    
    public QueueException(ErrorCode errorCode) {
        super(errorCode);
    }
}