# 3. 람다
- 251028-java-adv3/src/lambda/InstanceMain.java
- 람다를 사용하면 익명 클래스 사용 때의 보일러플레이트 코드를 줄일 수 있다.
- 함수형 인터페이스일 때 람다가 익명 클래스를 대체할 수 있다.
- 람다도 익명 클래스처럼 인스턴스가 생성된다.
- 문법 설탕(코드를 간결하게 만드는 문법적 편의) - 람다가 익명 클래스의 문법 설탕처럼 사용될 수 있다.
## 함수형 인터페이스
- 추상 메서드 한 개만 가지는 인터페이스를 말한다.
- 단일 추상 메서드 = SAM (Single Abstract Method)
- 람다는 클래스, 추상 클래스에는 할당할 수 없다.
- 람다는 함수형 인터페이스에만 할당할 수 있다.
```java
public interface NotSamInterface {
    void run();
    void go();
}
// run, go 앞에 abstract가 생략돼 있다. (추상 메서드)
```
- @FunctionalInterface: 컴파일 시점에 단일 추상 메서드인지 체크한다.
```java
@FunctionalInterface
public interface SamInterface {
    void run();
}
```
- 251028-java-adv3/src/lambda2/Main3.java
- 람다를 대입하는 것은 람다 인스턴스의 참조값을 대입하는 것이다.
- 람다 인스턴스를 인자로 해서 메서드에 전달할 수도 있고, 메서드가 람다 인스턴스를 반환할 수도 있다.
## 고차 함수 (Higher-Order Function)
- 함수를 다루는 추상화 수준이 더 높다.
- 보통 함수는 데이터(값)을 입력 받고, 값을 반환한다.
- 고차 함수는 함수를 인자로 받거나 함수를 반환한다.