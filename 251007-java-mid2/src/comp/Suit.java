package comp;

public enum Suit {
    SPADES,
    HEARTS,
    DIAMONDS,
    CLUBS;
    public String getIcon() {
        return switch (this) {
            case SPADES -> "♠";
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
        };
    }
}
