package time;
import java.time.*;

public class Main {
    public static void main(String[] args) {
        int year = 2025;
        for (int month = 1; month <= 12; month++) {
            LocalDate date = LocalDate.of(year, month, 1);
            int lastDay = date.lengthOfMonth();
            System.out.printf("%d년 %d월의 마지막 날은 %d일%n", year, month, lastDay);
        }
    }
}
