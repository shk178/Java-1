package lambda3;

import java.util.function.Predicate; // 입력o, 반환boolean
// Predicate - 조건 검사, 필터링 용도
import java.util.function.UnaryOperator; // 입력o, 반환o
import java.util.function.BinaryOperator; // 입력o, 반환o
// Operator - 동일한 타입의 연산 수행, 입력과 같은 타입 반환
// Unary 단항: 하나의 피연산자 - Funtion<T, T>를 상속받는다.
// Binary 이항: 두 개의 피연산자 - BiFunction<T, T, T> 상속

public class UtilMain2 {
    public static void main(String[] args) {
        Predicate<Integer> oddPredicate = n -> n % 2 != 0;
        System.out.println(oddPredicate.test(1)); // true
        UnaryOperator<Integer> increaseOper = n -> n + 1;
        System.out.println(increaseOper.apply(1)); // 2
        BinaryOperator<Integer> addOper = (x, y) -> x + y;
        System.out.println(addOper.apply(1, 2)); // 3
    }
}
