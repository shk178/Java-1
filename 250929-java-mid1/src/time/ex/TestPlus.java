package time.ex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class TestPlus {
    public static void main(String[] args) {
        LocalDateTime l = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        System.out.println(l.plusYears(1).plusMonths(2).plusDays(3).plusHours(4));
        LocalDate l2 = LocalDate.of(2024, 1, 1);
        int[] loop = new int[]{0, 1, 2, 3, 4};
        for (int i : loop) {
            LocalDate l3 = l2.plusWeeks(2 * i);
            System.out.println("날짜 " + (i + 1) + ": " + l3);
        }
    }
}
