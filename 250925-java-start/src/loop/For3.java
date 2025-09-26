package loop;

public class For3 {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1;; i++) {
            sum = sum + i;
            if (sum > 10) {
                System.out.println("i = " + i);
                System.out.println("sum = " + sum);
                break;
            }
            System.out.println("i = " + i);
            System.out.println("sum = " + sum);
        }
    }
}
