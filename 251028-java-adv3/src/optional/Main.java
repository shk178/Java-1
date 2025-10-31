package optional;

import java.util.Optional;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args) {
        Optional l = Optional.of("str1"); // raw 타입 Optional
        Optional<String> ll = Optional.of("str2"); // 제네릭 타입 명시하는 게 낫다.
        Optional<String> lll = Optional.ofNullable(null);
        Supplier<? extends Optional<? extends String>> supplier = () -> Optional.of("none");
        System.out.println(l.or(supplier)); // Optional[str1]
        System.out.println(ll.or(supplier)); // Optional[str2]
        System.out.println(lll.or(supplier)); // Optional[none]
    }
}
