package extends1.child;
import extends1.parent.Parent;

public class Child extends Parent {
    public void call() {
        printParent();
        System.out.println(getPublicValue());
        System.out.println(getProtectedValue());
        //System.out.println(getDefaultValue());
        //System.out.println(getPrivateValue());
    }
}
