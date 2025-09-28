package extends1.access;

public class Child2 extends Parent2 {
    public void call() {
        printParent();
        System.out.println(getPublicValue());
        System.out.println(getProtectedValue());
        System.out.println(getDefaultValue());
        //System.out.println(getPrivateValue());
    }
}
