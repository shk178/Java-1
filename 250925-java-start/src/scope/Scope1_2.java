package scope;

public class Scope1_2 {
    public static void main(String[] args) {
        int m = 10;
        int x = 10;
        if (true) {
            m = 20;
            x = 20;
            System.out.println("m = " + m);
            System.out.println("x = " + x);
        }
        System.out.println("m = " + m);
        System.out.println("x = " + x);
    }
}
