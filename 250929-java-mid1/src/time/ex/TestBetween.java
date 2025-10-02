package time.ex;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TestBetween {
    public static void main(String[] args) {
        LocalDate from = LocalDate.of(2025, 10, 2);
        LocalDate to1 = LocalDate.of(2025, 10, 3);
        LocalDate to2 = LocalDate.of(2025, 11, 3);
        LocalDate from2 = LocalDate.of(2025, 11, 2);
        LocalDate to3 = LocalDate.of(2026, 9, 1);
        LocalDate to4 = LocalDate.of(2025, 12, 3);
        System.out.println(ChronoUnit.DAYS.between(from, to1)); //1
        System.out.println(ChronoUnit.DAYS.between(from, to2)); //32
        System.out.println(ChronoUnit.DAYS.between(from2, to3)); //303
        System.out.println(ChronoUnit.DAYS.between(from2, to4)); //31
    }
}
