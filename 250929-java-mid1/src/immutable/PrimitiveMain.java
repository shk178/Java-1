package immutable;

public class PrimitiveMain {
    public static void main(String[] args) {
        int a = 10;
        int b = a; //복사해서 대입
        System.out.println("a = " + a);
        System.out.println("b = " + b);
        a = 20;
        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
}
