package optional;

import java.util.Optional;
import java.util.function.Supplier;

public class Evaluation3 {
    public static void main(String[] args) {
        Optional<Integer> n = Optional.of(100);
        Optional<Integer> nn = Optional.empty();
        System.out.println("n.orElse(two())=" + n.orElse(two()));
        //two 실행
        //n.orElse(two())=100
        System.out.println("nn.orElse(two())=" + nn.orElse(two()));
        //two 실행
        //nn.orElse(two())=2
        System.out.println("n.orElseGet(() -> two())=" + n.orElseGet(() -> two()));
        //n.orElseGet(() -> two())=100
        System.out.println("nn.orElseGet(() -> two())=" + nn.orElseGet(() -> two()));
        //two 실행
        //nn.orElseGet(() -> two())=2
        Supplier<Integer> supplier = () -> two();
        System.out.println("n.orElse(supplier.get())=" + n.orElse(supplier.get()));
        //two 실행
        //n.orElse(supplier.get())=100
        System.out.println("nn.orElse(supplier.get())=" + nn.orElse(supplier.get()));
        //two 실행
        //nn.orElse(supplier.get())=2
    }
    static int two() {
        System.out.println("two 실행");
        return 2;
    }
}
