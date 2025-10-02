package time.ex2;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class Cal {
    public static void main(String[] args) {
        int year = 2025;
        int month = 10;
        DateTimeFormatter f = DateTimeFormatter.ofPattern("d");
        LocalDate l = LocalDate.of(year, month, 1);
        LocalDate l2 = l.with(TemporalAdjusters.lastDayOfMonth());
        int last = l2.getDayOfMonth();
        String[] arr = {"Su ", "Mo ", "Tu ", "We ", "Th ", "Fr ", "Sa "};
        for (String s : arr) {
            System.out.print(s);
        }
        System.out.println();
        int offset = DayOfWeek.from(l).getValue() % 7; //"Su "면 0칸 띄운다.
        int count = offset;
        for (int i = 0; i < offset; i++) {
            System.out.print("   ");
        }
        for (int j = 1; j <= last; j++) {
            if (count % 7 == 0) {
                System.out.println();
            }
            System.out.printf("%2d ", j);
            count++;
        }
    }
}
