package autoclose;

public class Main2 {
    public static void main(String[] args) {
        try {
            logic();
        } catch (CallException e) {
            System.out.println("callEx 예외 처리");
        } catch (CloseException e) {
            System.out.println("closeEx 예외 처리");
        }
    }
    private static void logic() throws CallException, CloseException {
        Resource one = new Resource("r1");
        Resource two = new Resource("r2");
        try {
            one.call();
            two.callEx(); // try 블록은 중단되고 → finally 블록으로 이동
        } finally { // finally 블록 실행
            one.close();
            two.closeEx(); // 새로운 예외 (CloseException)이 던져지며 호출한 쪽 (main)으로 전달
            two.callEx(); // 실행 안 됨
        }
    }
}
/*
r1 call
r2 callEx
r1 close
r2 closeEx
closeEx 예외 처리 // callEx()에서 던진 예외가 finally 블록의 closeEx()에서 새 예외로 덮어씌워졌기 때문
 */