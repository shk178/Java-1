package spring.tx;

public class BzException extends Exception {
    public BzException() {
        super();
    }

    public BzException(Throwable cause) {
        super(cause);
    }

    public BzException(String message) {
        super(message);
    }

    public BzException(String message, Throwable cause) {
        super(message, cause);
    }
}
