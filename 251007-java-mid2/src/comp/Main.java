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
이름1: [♥4, ♦4, ♣8, ♠10, ♠11]
이름2: [♣5, ♦7, ♦11, ♦12, ♣12]
이름1: [R1: lose(37)]
이름2: [R1: win(47)]
이름1: [♥1, ♠2, ♣2, ♠7, ♣9]
이름2: [♦6, ♣7, ♦8, ♥9, ♣11]
이름1: [R1: lose(37), R2: lose(21)]
이름2: [R1: win(47), R2: win(41)]
이름1: [♥2, ♥5, ♦5, ♦7, ♦11]
이름2: [♠3, ♦3, ♦4, ♣4, ♠10]
이름1: [R1: lose(37), R2: lose(21), R3: win(30)]
이름2: [R1: win(47), R2: win(41), R3: lose(24)]
이름1: [♠5, ♠9, ♥9, ♠10, ♦12]
이름2: [♠1, ♣2, ♦5, ♣8, ♥12]
이름1: [R1: lose(37), R2: lose(21), R3: win(30), R4: win(45)]
이름2: [R1: win(47), R2: win(41), R3: lose(24), R4: lose(28)]
이름1: [♦2, ♥3, ♠5, ♠7, ♥8]
이름2: [♦1, ♥2, ♦3, ♠12, ♥12]
이름1: [R1: lose(37), R2: lose(21), R3: win(30), R4: win(45), R5: lose(25)]
이름2: [R1: win(47), R2: win(41), R3: lose(24), R4: lose(28), R5: win(30)]
 */