package chars;

import java.nio.charset.Charset;

public class DefaultSet {
    public static void main(String[] args) {
        System.out.println(Charset.defaultCharset());
        // UTF-8 (OS 환경에 따라 다름)
    }
}
