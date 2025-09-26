package loop;

public class For1 {
    public static void main(String[] args) {
        int sum = 0;
        int endNum = 10;
        for (int i = 1; i <= endNum; i++) {
            sum = sum + i;
            System.out.println("i = " + i);
            System.out.println("sum = " + sum);
        }
    }
}
