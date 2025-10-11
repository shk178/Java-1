package comp;
import java.util.*;

public class Player {
    private String name;
    private List<Card> hand;
    private int score;
    private List<String> sheet;
    Player(String name) {
        this.name = name;
        sheet = new ArrayList<>();
    }
    //5장 한 번에 받기
    public void setHand(List<Card> cards) {
        this.hand = cards;
    }
    //카드 정렬 (숫자 + 무늬 우선순위)
    private void sortHand() {
        Collections.sort(hand);
    }
    //카드 목록 반환
    public List<Card> getHand() {
        System.out.print(name + ": ");
        sortHand();
        return hand;
    }
    //총 점수 계산
    public int calculateScore() {
        score = 0;
        for (Card card : hand) {
            score += card.getNumber();
        }
        return score;
    }
    //결과 기록
    public void setSheet(String result) {
        sheet.add(result);
    }
    //결과 목록 반환
    public List<String> getSheet() {
        System.out.print(name + ": ");
        return sheet;
    }
}
