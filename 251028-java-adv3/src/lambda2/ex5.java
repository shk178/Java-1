package lambda2;

public class ex5 {
    public static void main(String[] args) {
        MyTransformer f1 = s -> s.toUpperCase();
        MyTransformer f2 = s -> "**" + s + "**";
        MyTransformer f3 = compose(f1, f2);
        System.out.println(f3.transform("hello")); // **HELLO**
    }
    static MyTransformer compose(MyTransformer f1, MyTransformer f2) {
        return (s) -> {
            return f2.transform(f1.transform(s));
        };
    }
}
