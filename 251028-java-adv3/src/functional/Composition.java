package functional;

import java.util.function.Function;

public class Composition {
    public static void main(String[] args) {
        Function<Integer, Integer> square = x -> x * x;
        Function<Integer, Integer> increment = x -> x + 1;
        //1. compose()로 함수 합성
        //increment -> square
        Function<Integer, Integer> newFunct1 = square.compose(increment);
        System.out.println(newFunct1.apply(3)); // 16
        //2. andThen()으로 함수 합성
        //square -> increment
        Function<Integer, Integer> newFunct2 = square.andThen(increment);
        System.out.println(newFunct2.apply(3)); // 10
    }
}
