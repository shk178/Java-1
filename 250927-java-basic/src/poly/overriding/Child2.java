package poly.overriding;

public class Child2 extends Parent {
    public String value = "child2";
    @Override
    public void method() {
        System.out.println("child2 method");
    }
}
