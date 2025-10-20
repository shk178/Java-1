package chars;

public class TC {
    public static void main(String[] args) {
        int a = -5;
        byte b = -5;
        int c = 5;
        byte d = 5;
        System.out.println(Integer.toBinaryString(a));
        // 11111111111111111111111111111011
        System.out.println(Integer.toBinaryString(b));
        // 11111111111111111111111111111011
        System.out.println(Integer.toBinaryString(c));
        // 101
        System.out.println(Integer.toBinaryString(d));
        // 101
        System.out.println(Integer.toBinaryString(0xFF));
        // 11111111
        System.out.println(Integer.toBinaryString(a & 0xFF));
        // 11111011
        System.out.println(Integer.toBinaryString(b & 0xFF));
        // 11111011
    }
}