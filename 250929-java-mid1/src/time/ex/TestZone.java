package time.ex;
import java.time.ZonedDateTime;
import java.time.ZoneId;

public class TestZone {
    public static void main(String[] args) {
        ZonedDateTime z = ZonedDateTime.of(2024, 1, 1, 9,
                0, 0, 0, ZoneId.of("Asia/Seoul"));
        ZonedDateTime z2 = z.withZoneSameInstant(ZoneId.of("Europe/London"));
        ZonedDateTime z3 = z.withZoneSameInstant(ZoneId.of("America/New_York"));
        System.out.println(z);
        System.out.println(z2);
        System.out.println(z3);
    }
}
