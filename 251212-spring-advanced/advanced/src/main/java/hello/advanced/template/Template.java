package hello.advanced.template;

public class Template {
    public void execute(Callback callback) {
        long sTime = System.currentTimeMillis();
        callback.call();
        long eTime = System.currentTimeMillis();
        System.out.println("duration = " + (eTime - sTime));
    }
}
// 다른 코드의 인수로 넘겨주는, 실행 가능한 코드를 콜백이라고 한다.
// 메서드 파라미터 전략 객체도 콜백이다. (Context -> Template, Strategy -> Callback)
// 코드를 인수로 넘기는 방법: 익명 내부 클래스, 람다