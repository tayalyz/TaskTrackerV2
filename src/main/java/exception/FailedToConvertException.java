package exception;

public class FailedToConvertException extends RuntimeException{

    public FailedToConvertException(String message) {
        super(message);
    }
}
