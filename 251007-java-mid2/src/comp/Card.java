package comp;

public class Card implements Comparable<Card> {
    private int number;
    private Suit suit;
    public Card(int number, Suit suit) {
        this.number = number;
        this.suit = suit;
    }
    @Override
    public int compareTo(Card other) {
        int compareNum = this.number - other.number;
        if (compareNum != 0) {
            return compareNum;
        } else {
            return this.suit.ordinal() - other.suit.ordinal();
        }
    }
    //카드 숫자 반환
    public int getNumber() {
        return number;
    }
    //카드 무늬 반환
    public Suit getSuit() {
        return suit;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(suit.getIcon());
        sb.append(number);
        return sb.toString();
    }
}
