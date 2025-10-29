package lambda3;

public class GenericMain {
    public static void main(String[] args) {
        StringFunction upperCase = s -> s.toUpperCase();
        String result1 = upperCase.apply("hello");
        NumberFunction square = n -> (int) n * (int) n;
        int result2 = (int) square.apply(3);
        GenericFunction<String, Integer> strLen = s -> s.length();
        int result3 = strLen.apply("world");
        System.out.println(result1); // HELLO
        System.out.println(result2); // 9
        System.out.println(result3); // 5
    }
}
