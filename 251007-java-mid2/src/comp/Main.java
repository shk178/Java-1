package comp;
/*
- 카드는 1부터 13까지 있다.
- 번호당 스페이드, 하트, 다이아, 클로버 문양이 있다. (총 52장)
- Deck = 52장 카드 뭉치
- 2명의 Player가 진행
- step 1. 덱의 카드 랜덤 섞는다.
- step 2. 각 플레이어가 덱에서 카드 5장씩 뽑는다.
- step 3. 5장 카드를 정렬된 순서대로 오픈한다.
- 작은 숫자 먼저, 같은 숫자는 스페이드️♠️->하트♥️->다이아♦️->클로버♣️ 순
- step 4. 카드 숫자 합계가 큰 플레이어가 승리한다. 같으면 무승부
 */
public class Main {
    public static void main(String[] args) {
        Player player1 = new Player("이름1");
        Player player2 = new Player("이름2");
        Game game = new Game(player1, player2, 0);
        game.playRound();
        game.playRound();
        game.playRound();
        game.playRound();
        game.playRound();
    }
}
/*
이름1: [♥3, ♣4, ♣6, ♦9, ♦11]
이름2: [♥10, ♥11, ♣11, ♠12, ♥12]
이름1: [R1: win(33)]
이름2: [R1: lose(56)]
이름1: [♣4, ♥6, ♥9, ♠10, ♥10]
이름2: [♦4, ♦6, ♦9, ♣9, ♠12]
이름1: [R1: win(33), R2: win(39)]
이름2: [R1: lose(56), R2: lose(40)]
이름1: [♠1, ♣1, ♣9, ♠10, ♣11]
이름2: [♦5, ♣5, ♠7, ♦8, ♥11]
이름1: [R1: win(33), R2: win(39), R3: win(32)]
이름2: [R1: lose(56), R2: lose(40), R3: lose(36)]
이름1: [♣1, ♦2, ♠6, ♦6, ♣7]
이름2: [♠1, ♦1, ♠3, ♣6, ♣11]
이름1: [R1: win(33), R2: win(39), R3: win(32), R4: draw(22)]
이름2: [R1: lose(56), R2: lose(40), R3: lose(36), R4: draw(22)]
이름1: [♠3, ♣3, ♥4, ♦4, ♥5]
이름2: [♥1, ♥3, ♦7, ♣7, ♦10]
이름1: [R1: win(33), R2: win(39), R3: win(32), R4: draw(22), R5: win(19)]
이름2: [R1: lose(56), R2: lose(40), R3: lose(36), R4: draw(22), R5: lose(28)]
 */