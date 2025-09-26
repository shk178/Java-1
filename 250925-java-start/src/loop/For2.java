package loop;

public class For2 {
    public static void main(String[] args) {
        int sum = 0;
        int i = 1;
        for (;;) {
            sum = sum + i;
            if (sum > 10) {
                System.out.println("i = " + i);
                System.out.println("sum = " + sum);
                break;
            }
            System.out.println("i = " + i);
            System.out.println("sum = " + sum);
            i++;
        }
    }
}
