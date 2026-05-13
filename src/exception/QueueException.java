package exception;

public class QueueException extends DutyFreeException {

    public QueueException(String errorCode, String logMessage) {
        super(errorCode, logMessage);
    }
    
    public QueueException(ErrorCode errorCode) {
        super(errorCode);
    }
}