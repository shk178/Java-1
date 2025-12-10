package jdbc.except;

public class MyDBException2 extends MyDBException {
    public MyDBException2() {
        super();
    }

    public MyDBException2(Throwable cause) {
        super(cause);
    }

    public MyDBException2(String message) {
        super(message);
    }

    public MyDBException2(String message, Throwable cause) {
        super(message, cause);
    }

    protected MyDBException2(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
