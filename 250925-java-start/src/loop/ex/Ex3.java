package loop.ex;

public class Ex3 {
    public static void main(String[] args) {
        int sum = 0;
        int max = 100;
        int i = 1;
        while (i <= max) {
            sum += i;
            i++;
        }
        System.out.println("sum = " + sum);
        sum = 0;
        i = 1;
        int diff = 1;
        for (; i <= max; i += diff) {
            sum += i;
        }
        System.out.println("sum = " + sum);
    }
}
