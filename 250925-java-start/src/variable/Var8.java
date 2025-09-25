package variable;

public class Var8 {
    public static void main(String[] args) {
        //정수
        byte b = 127; //-128 ~ 127 (1byte, 2^8)
        short s = 32767; //-32,768 ~ 32,767 (2byte, 2^16)
        int i = 2147483647; //-2,147,483,648 ~ 2,147,483,647 (약 20억) (4byte, 2^32)
        //-9,223,372,036,854,775,808 ~ 9,223,372,036,854,775,807 (8byte, 2^64)
        long l = 9223372036854775807L;

        //실수
        float f = 10.0f; //대략 -3.4E38 ~ 3.4E38, 7자리 정밀도 (4byte, 2^32)
        double d = 10.0; //대략 -1.7E308 ~ 1.7E308, 15자리 정밀도 (8byte, 2^64)

        //boolean: true, false (1byte)
        //char: 문자 하나 (2byte)
        //String: 문자열 길이에 따라 byte(메모리 사용량) 달라짐
    }
}
