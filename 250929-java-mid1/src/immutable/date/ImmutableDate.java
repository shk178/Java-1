package immutable.date;

public class ImmutableDate {
    private final int year;
    private final int month;
    private final int day;

    public ImmutableDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public String toString() {
        return "ImmutableDate{" +
                year +
                ", " + month +
                ", " + day +
                '}';
    }

    public ImmutableDate withYear(int newYear) {
        return new ImmutableDate(newYear, month, day);
    }

    public ImmutableDate withMonth(int newMonth) {
        return new ImmutableDate(year, newMonth, day);
    }

    public ImmutableDate withDay(int newDay) {
        return new ImmutableDate(year, month, newDay);
    }
}
