package optional;

import java.util.Optional;

public class Main2 {
    public static void main(String[] args) {
        Optional<String> l = Optional.of("str3");
        Optional<String> ll = Optional.empty();
        l.ifPresentOrElse(
                s -> System.out.println(s),
                () -> System.out.println("none")
        ); // str3
        ll.ifPresentOrElse(
                s -> System.out.println(s),
                () -> System.out.println("none")
        ); // none
        Optional<Integer> li = l.map(String::length);
        Optional<Integer> lli = ll.map(String::length);
        System.out.println(li); // Optional[4]
        System.out.println(lli); // Optional.empty
        Optional<Optional<String>> ln = l.map(s -> Optional.of(s));
        Optional<Optional<String>> lln = ll.map(s -> Optional.of(s));
        System.out.println(ln); // Optional[Optional[str3]]
        System.out.println(lln); // Optional.empty
        Optional<String> lf = l.flatMap(s -> Optional.of(s));
        Optional<String> llf = ll.flatMap(s -> Optional.of(s));
        System.out.println(lf); // Optional[str3]
        System.out.println(llf); // Optional.empty
    }
}
