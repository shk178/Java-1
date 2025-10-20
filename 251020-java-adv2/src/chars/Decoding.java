package chars;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Decoding {
    private static final Charset ECU_KR = Charset.forName("EUC-KR");
    private static final Charset MS_949 = Charset.forName("MS949");
    public static void main(String[] args) {
        encodeDecode("A");
        encodeDecode("a");
        encodeDecode("&");
        encodeDecode("'");
        encodeDecode("ㄱ");
        encodeDecode("가");
        encodeDecode("깤");
    }
    private static void encodeDecode(String text) {
        byte[] bytes1 = text.getBytes(ECU_KR);
        byte[] bytes2 = text.getBytes(MS_949);
        System.out.printf("%s [%s 인코딩] %s (%sbyte) ", text, ECU_KR, Arrays.toString(bytes1), bytes1.length);
        System.out.printf("[%s 디코딩] %s\n", ECU_KR, new String(bytes1, ECU_KR));
        System.out.printf("%s [%s 인코딩] %s (%sbyte) ", text, MS_949, Arrays.toString(bytes2), bytes2.length);
        System.out.printf("[%s 디코딩] %s\n", MS_949, new String(bytes2, MS_949));
    }
}
/*
A [EUC-KR 인코딩] [65] (1byte) [EUC-KR 디코딩] A
A [x-windows-949 인코딩] [65] (1byte) [x-windows-949 디코딩] A
a [EUC-KR 인코딩] [97] (1byte) [EUC-KR 디코딩] a
a [x-windows-949 인코딩] [97] (1byte) [x-windows-949 디코딩] a
& [EUC-KR 인코딩] [38] (1byte) [EUC-KR 디코딩] &
& [x-windows-949 인코딩] [38] (1byte) [x-windows-949 디코딩] &
' [EUC-KR 인코딩] [39] (1byte) [EUC-KR 디코딩] '
' [x-windows-949 인코딩] [39] (1byte) [x-windows-949 디코딩] '
ㄱ [EUC-KR 인코딩] [-92, -95] (2byte) [EUC-KR 디코딩] ㄱ
ㄱ [x-windows-949 인코딩] [-92, -95] (2byte) [x-windows-949 디코딩] ㄱ
가 [EUC-KR 인코딩] [-80, -95] (2byte) [EUC-KR 디코딩] 가
가 [x-windows-949 인코딩] [-80, -95] (2byte) [x-windows-949 디코딩] 가
깤 [EUC-KR 인코딩] [63] (1byte) [EUC-KR 디코딩] ? // 인코딩 디코딩 안 됨
깤 [x-windows-949 인코딩] [-125, -105] (2byte) [x-windows-949 디코딩] 깤
 */