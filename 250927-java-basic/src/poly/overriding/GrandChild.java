package poly.overriding;

public class GrandChild extends Child {
    public String value = "grandChild";
    @Override
    public void method() {
        System.out.println("grandChild method");
    }
}
