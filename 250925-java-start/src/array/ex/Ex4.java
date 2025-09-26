package array.ex;
import java.util.Scanner;

public class Ex4 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("학생 수: ");
        int count1 = scanner.nextInt();
        System.out.print("과목 수: ");
        int count2 = scanner.nextInt();
        scanner.nextLine();
        String[] subjects = new String[count2];
        for (int i = 0; i < subjects.length; i++) {
            System.out.print("과목명: ");
            subjects[i] = scanner.nextLine();
        }
        int[][] scores = new int[count1][count2];
        for (int j = 0; j < scores.length; j++) {
            int k = 0;
            int sum = 0;
            for (String subject : subjects) {
                System.out.print(subject + ": ");
                int score = scanner.nextInt();
                scores[j][k] = score;
                sum += score;
                k++;

            }
            double avg = (double) sum / subjects.length;
            System.out.println((j + 1) + "번 학생");
            System.out.println("sum = " + sum);
            System.out.println("avg = " + avg);
        }
    }
}
