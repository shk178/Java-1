package scanner.ex;
import java.util.Scanner;

public class Ex6 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        double sum = 0;
        double average = 0;
        int count = 0;
        while (true) {
            System.out.print("num: ");
            double temp = input.nextDouble();
            if (temp == -1) {
                break;
            } else {
                sum += temp;
                count++;
            }
        }
        average = sum / count;
        System.out.println("count = " + count);
        System.out.println("sum = " + sum);
        System.out.println("average = " + average);
    }
}
