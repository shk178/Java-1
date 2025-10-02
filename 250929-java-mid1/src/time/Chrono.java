package time;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class Chrono {
    public static void main(String[] args) {
        ChronoUnit[] chronoUnits = ChronoUnit.values();
        for (ChronoUnit chronoUnit : chronoUnits) {
            System.out.print(chronoUnit + ", ");
        }
        System.out.println();
        ChronoField[] chronoFields = ChronoField.values();
        for (ChronoField chronoField : chronoFields) {
            System.out.print(chronoField + ", ");
        }
    }
}
