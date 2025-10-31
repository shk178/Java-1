package optional;

public class Evaluation2 {
    public static void main(String[] args) {
        LambdaDebug log = new LambdaDebug();
        log.setDebug(true);
        log.debug(() -> one());
        //one 호출
        //[DEBUG] 1
        log.setDebug(false);
        log.debug(() -> one()); // one() 실행 안 됨
    }
    static int one() {
        System.out.println("one 호출");
        return 1;
    }
}
