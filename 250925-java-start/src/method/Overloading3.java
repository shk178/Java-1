package method;

public class Overloading3 {
    public static void main(String[] args) {
        System.out.println(add(3, 4));
        //System.out.println(add(4.0, 5.0));
    }
    public static double add(int a, double b) {
        return a + b;
    }
}
