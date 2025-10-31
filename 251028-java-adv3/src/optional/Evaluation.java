package optional;

public class Evaluation {
    public static void main(String[] args) {
        DebugLogger log = new DebugLogger();
        log.setDebug(true);
        log.debug(10 + 20); // 10 + 20 계산 후 debug 호출 - print 호출됨
        log.setDebug(false);
        log.debug(10 + 20); // 10 + 20 계산 후 debug 호출 - print 호출x
        // 10 + 20은 항상 실행된다.
        // 10 + 20이 실행 안 되도록 연산 정의 시점과 실행 시점을 분리한다.
    }
}
