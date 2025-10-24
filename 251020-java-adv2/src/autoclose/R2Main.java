package autoclose;

public class R2Main {
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
        try (Resource2 r2one = new Resource2("r2-1");
        Resource2 r2two = new Resource2("r2-2")) {
            r2one.call();
            r2two.callEx(); // try-with-resources는 예외가 발생하면 즉시 close()를 호출함
        } catch (CallException e) {
            System.out.println("ex: " + e); // ex: autoclose.CallException: r2-2 ex
            throw e; // callEx 예외 처리
        }
    }
}
/*
r2-1 call
r2-2 callEx
r2-2 close
r2-1 close
ex: autoclose.CallException: r2-2 ex
callEx 예외 처리
 */
//자바의 try-with-resources가 예외를 자동으로 억제(suppressed)하기 때문
//close()에서 예외가 발생했지만 표면적으로 던져지지 않고 숨겨진 상태로 있다.
//catch (CallException e)에서 getSuppressed()로 확인할 수 있다.