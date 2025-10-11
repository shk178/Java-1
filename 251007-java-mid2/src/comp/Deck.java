package comp;
import java.util.*;

public class Deck {
    private List<Card> deck;
    Deck() {
        deck = new ArrayList<>();
        initialize();
    }
    //52장 카드 생성
    private void initialize() {
        for (int i = 1; i < 13; i++) {
            for (Suit value : Suit.values()) {
                deck.add(new Card(i, value));
            }
        }
    }
    //카드 섞기
    public void shuffle() {
        Collections.shuffle(deck);
    }
    //카드 한 장 뽑기
    private Card draw() {
        return deck.removeLast();
    }
    //여러 장 한꺼번에 뽑기
    public List<Card> drawMultiple(int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(draw());
        }
        return cards;
    }
}
