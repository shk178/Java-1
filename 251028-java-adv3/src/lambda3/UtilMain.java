package lambda3;

import java.util.function.Function; // 입력, 반환
//Function - 데이터 변환, 필드 추출 등
import java.util.function.Consumer; // 입력
//Consumer - 로그 출력, DB 저장 등
import java.util.function.Supplier; // 반환
//Supplier - 객체 생성, 값 반환 등
// Runnable은 입력x, 반환x
//Runnable - 스레드 실행 (멀티스레드)

public class UtilMain {
    public static void main(String[] args) {
        Function<String, String> world = s -> s + " world";
        System.out.println(world.apply("hello")); // hello world
        Function<Integer, Integer> f1 = i -> i + i;
        Function<Integer, Integer> f2 = f1;
        System.out.println(f2.apply(3)); // 6
        Consumer<String> printConsume = s -> {
            System.out.println(s);
        };
        Supplier<String> helloSupply = () -> {
            return "hello";
        };
        Runnable runnable = () -> {
            System.out.println("runnable.run");
        };
        printConsume.accept("hello"); // hello
        System.out.println(helloSupply.get()); // hello
        runnable.run(); // runnable.run
    }
}
