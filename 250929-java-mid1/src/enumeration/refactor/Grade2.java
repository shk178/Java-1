package enumeration.refactor;

public enum Grade2 {
    BASIC(10), GOLD(20), DIA(30);
    private final int discountPercent;
    Grade2(int percent) {
        this.discountPercent = percent;
    }
    public int discountWon(int price) {
        return price * this.discountPercent / 100;
    }
}
