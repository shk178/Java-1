package chars;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Encoding {
    private static final Charset ECU_KR = Charset.forName("EUC-KR");
    private static final Charset MS_949 = Charset.forName("MS949");
    public static void main(String[] args) {
        encode("A");
        encode("a");
        encode("&");
        encode("'");
        encode("ㄱ");
        encode("가");
    }
    private static void encode(String text) {
        byte[] bytes1 = text.getBytes(ECU_KR);
        byte[] bytes2 = text.getBytes(MS_949);
        System.out.printf("%s [%s 인코딩] %s %sbyte / ", text, ECU_KR, Arrays.toString(bytes1), bytes1.length);
        System.out.printf("[%s 인코딩] %s %sbyte\n", MS_949, Arrays.toString(bytes2), bytes2.length);
    }
}
/*
A [EUC-KR 인코딩] [65] 1byte / [x-windows-949 인코딩] [65] 1byte
a [EUC-KR 인코딩] [97] 1byte / [x-windows-949 인코딩] [97] 1byte
& [EUC-KR 인코딩] [38] 1byte / [x-windows-949 인코딩] [38] 1byte
' [EUC-KR 인코딩] [39] 1byte / [x-windows-949 인코딩] [39] 1byte
ㄱ [EUC-KR 인코딩] [-92, -95] 2byte / [x-windows-949 인코딩] [-92, -95] 2byte
가 [EUC-KR 인코딩] [-80, -95] 2byte / [x-windows-949 인코딩] [-80, -95] 2byte
 */