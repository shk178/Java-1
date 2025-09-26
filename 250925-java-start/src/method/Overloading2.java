package method;

public class Overloading2 {
    public static void main(String[] args) {
        System.out.println(add(1, 2.0));
        System.out.println(add(2.0, 3));
        //System.out.println(add(3, 4));
        //System.out.println(add(4.0, 5.0));
    }
    public static double add(int a, double b) {
        return a + b;
    }
    public static double add(double b, int a) {
        return a + b;
    }
}
