package time;
import java.time.*;

public class Main2 {
    public static void main(String[] args) {
        LocalDate d1 = LocalDate.of(2025, 10, 2);
        LocalDate d2 = LocalDate.of(2025, 10, 2);
        System.out.println(d1.equals(d2)); //true
        System.out.println(d1.isEqual(d2)); //true
        ZonedDateTime z1 = ZonedDateTime.of(2025, 10, 2, 0, 0, 0, 0, ZoneId.of("Asia/Seoul"));
        ZonedDateTime z2 = ZonedDateTime.of(2025, 10, 1, 15, 0, 0, 0, ZoneId.of("UTC"));
        System.out.println(z1.equals(z2)); //false (타임존까지 달라서 객체 값이 다름)
        System.out.println(z1.isEqual(z2)); //true (실제로 같은 순간을 가리킴)
    }
}
