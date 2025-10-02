package time;
import java.time.*;

public class Amount {
    public static void main(String[] args) {
        Period p = Period.ofDays(10);
        System.out.println(p);
        LocalDate l = LocalDate.now();
        System.out.println(l);
        LocalDate l10 = l.plus(p);
        System.out.println(l10);
        System.out.println(Period.between(l, l10));
    }
}
