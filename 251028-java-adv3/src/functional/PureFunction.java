package functional;

import java.util.function.Function;

public class PureFunction {
    public static int count = 0;
    public static void main(String[] args) {
        Function<Integer, Integer> function = x -> x * 2;
        System.out.println(function.apply(10)); //20
        System.out.println(function.apply(10)); //20
        Function<Integer, Integer> notPureFunct = x -> {
            count++;
            //여기에 sout을 써도 콘솔(외부세계)에 출력(영향)이라서 부수 효과다.
            return x * 2;
        };
        System.out.println(notPureFunct.apply(10) + " " + count); // 20 1
        System.out.println(notPureFunct.apply(10) + " " + count); // 20 2
        // 외부 상태 변화함 = 부수 효과 발생
    }
}
