package method.ex;

public class Ex1 {
    public static void main(String[] args) {
        int a = 1, b = 2, c = 2;
        System.out.println(avg(a, b, c));
    }
    public static double avg(int a, int b, int c) {
        return (double) (a + b + c) / 3;
    }
}
