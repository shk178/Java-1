package lambda2;

public class Main2 {
    public static void main(String[] args) {
        //<람다와 생략>
        // 표현식 = 하나의 값으로 표현되는 코드 조각
        // 산술 논리 표현식, 메서드 호출, 객체 생성 등
        // 표현식이 아님 = 제어문, 메서드 선언 등
        // 1. 단일 표현식의 경우 중괄호와 리턴 생략 가능
        // 중괄호 사용 시 리턴 포함해야 한다.
        MyFunction one = (int a, int b) -> a + b;
        // 2. 단일 표현식 아니면 중괄호, 리턴 필수
        MyFunction two = (int aa, int bb) -> {
            int y = aa + bb;
            return y;
        };
        // 3. 매개변수와 반환값의 타입 컴파일러가 추론한다.
        // 반환 타입은 항상 추론한다. 매개변수 타입 생략 가능
        MyFunction three = (aaa, bbb) -> aaa + bbb;
    }
}
