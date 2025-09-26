package scope;

public class Scope1_3 {
    public static void main(String[] args) {
        int m = 10;
        for (int x = 20; x <= 20; x++) {
            System.out.println("m = " + m);
            System.out.println("x = " + x);
        }
        System.out.println("m = " + m);
        //System.out.println("x = " + x);
    }
}
