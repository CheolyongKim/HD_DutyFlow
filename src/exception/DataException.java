package exception;

public class DataException extends DutyFreeException {

    public DataException(String errorCode, String message) {
        super(errorCode, message);
    }
}