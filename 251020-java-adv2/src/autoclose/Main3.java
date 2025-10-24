package autoclose;

public class Main3 {
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
            two.callEx();
        } finally {
            try { one.close(); } catch (Exception e) { System.out.println(e); }
            try { two.closeEx(); } catch (Exception e) { System.out.println(e); }
            try { two.callEx(); } catch (Exception e) { System.out.println(e); }
        }
    }
}
/*
r1 call
r2 callEx
r1 close
r2 closeEx
autoclose.CloseException: r2 ex // autoclose(패키지 이름)에 정의된 CloseException 클래스
// 패키지 포함된 클래스 이름을 클래스의 전체 이름(fully qualified name)이라고 한다.
// r2 ex: 예외 메시지 (super(msg)에 전달된 값)
r2 callEx
autoclose.CallException: r2 ex
callEx 예외 처리 // finally에서 새로운 예외가 던져지지 않는 한 try에서 발생했던 예외가 main으로 전파된다.
 */