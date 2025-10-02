package nested1;

public class Outer2 {
    private int secret = 42;
    static class Nested {
        Nested() {
            Outer2 outer = new Outer2();
            System.out.println(outer.secret); //된다.
            outer.hello(); //된다.
            outer.revealSecret(); //된다.
        }
    }
    private void revealSecret() {
        System.out.println(secret);
    }
    public void hello() {
        System.out.println("hello");
    }
}