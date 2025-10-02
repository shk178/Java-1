package time.ex;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class TestAdjusters {
    public static void main(String[] args) {
        int year = 2025;
        int month = 10;
        LocalDate l = LocalDate.of(year, month, 1);
        LocalDate l2 = l.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate l3 = l.with(TemporalAdjusters.lastDayOfMonth());
        System.out.println(DayOfWeek.from(l2));
        System.out.println(DayOfWeek.from(l3));
    }
}
