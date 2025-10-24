package autoclose;

public class Main {
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
        one.call();
        one.close();
        two.callEx();
        two.closeEx(); // 호출 안 됨
    }
}
/*
r1 call
r1 close
r2 callEx
callEx 예외 처리
 */