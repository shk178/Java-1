package casting;

public class Overflow1 {
    public static void main(String[] args) {
        long maxIntValue = 2147483647; //int 최대
        //long maxIntOver = 2147483648; //int 최대 + 1
        //long maxIntOver = (long)2147483648;
        long maxIntOver = 2147483648L;
        System.out.println("maxIntValue = " + maxIntValue);
        System.out.println("maxIntOver = " + maxIntOver);
    }
}
