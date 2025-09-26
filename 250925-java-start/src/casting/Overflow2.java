package casting;

public class Overflow2 {
    public static void main(String[] args) {
        long maxIntValue = 2147483647; //int 최대
        long maxIntOver = 2147483648L;
        int intValue1, intValue2, intValue3;
        intValue1 = (int)maxIntValue;
        intValue2 = (int)maxIntOver;
        intValue3 = (int)2147483649L;
        System.out.println("intValue1 = " + intValue1);
        System.out.println("intValue2 = " + intValue2);
        System.out.println("intValue3 = " + intValue3);
    }
}
