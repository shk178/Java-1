package casting;

public class Casting2 {
    public static void main(String[] args) {
        int intValue = 0;
        long longValue = 20L;
        double doubleValue = 1.5;
        double doubleValue2 = 1.12345678901234567890;
        longValue = (long)doubleValue;
        intValue = (int)longValue;
        System.out.println("intValue = " + intValue);
        System.out.println("longValue = " + longValue);
        System.out.println("doubleValue = " + doubleValue);
    }
}
