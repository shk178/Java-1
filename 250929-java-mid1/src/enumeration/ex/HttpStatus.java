package enumeration.ex;

public enum HttpStatus {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    private final int code;
    private final String msg;
    HttpStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public static HttpStatus findByCode(int codeValue) {
        for (HttpStatus value : values()) {
            if (value.code == codeValue)
                return value;
        }
        return null;
    }
}
