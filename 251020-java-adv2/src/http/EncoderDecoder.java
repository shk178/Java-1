package http;

import java.net.URLDecoder;
import java.net.URLEncoder;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EncoderDecoder {
    public static void main(String[] args) {
        String encode = URLEncoder.encode("가", UTF_8);
        System.out.println("encode = " + encode); // encode = %EA%B0%80
        String decode = URLDecoder.decode(encode, UTF_8);
        System.out.println("decode = " + decode); // decode = 가
    }
}
