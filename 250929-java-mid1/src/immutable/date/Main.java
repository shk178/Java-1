package immutable.date;

public class Main {
    public static void main(String[] args) {
        ImmutableDate date1 = new ImmutableDate(2025, 9, 30);
        System.out.println(date1.withYear(2026));
        System.out.println(date1.withMonth(10));
        System.out.println(date1.withDay(31));
        System.out.println("date1 = " + date1);
    }
}
