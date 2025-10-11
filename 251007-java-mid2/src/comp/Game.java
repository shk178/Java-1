package comp;

public class Game {
    private Deck deck;
    private Player player1;
    private Player player2;
    private int round;
    Game(Player player1, Player player2, int round) {
        this.player1 = player1;
        this.player2 = player2;
        this.round = round;
    }
    //게임 시작 (덱 생성, 섞기, 카드 배분)
    private void start() {
        Deck deck = new Deck();
        deck.shuffle();
        player1.setHand(deck.drawMultiple(5));
        player2.setHand(deck.drawMultiple(5));
    }
    //한 라운드 진행
    public void playRound() {
        round++;
        start();
        compareScores();
        printResult();
    }
    //승자 판별
    private void compareScores() {
        System.out.println(player1.getHand());
        System.out.println(player2.getHand());
        int score1 = player1.calculateScore();
        int score2 = player2.calculateScore();
        String result1;
        String result2;
        if (score1 > score2) {
            result1 = "win";
            result2 = "lose";
        } else if (score1 == score2) {
            result1 = "draw";
            result2 = "draw";
        } else {
            result1 = "win";
            result2 = "lose";
        }
        player1.setSheet("R"+round+": "+result1+"("+score1+")");
        player2.setSheet("R"+round+": "+result2+"("+score2+")");
    }
    //결과 출력
    private void printResult() {
        System.out.println(player1.getSheet());
        System.out.println(player2.getSheet());
    }
}
