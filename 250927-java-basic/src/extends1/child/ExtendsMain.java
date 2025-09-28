package extends1.child;

public class ExtendsMain {
    public static void main(String[] args) {
        Child child = new Child();
        child.call();
        child.printParent();
        System.out.println(child.getPublicValue());
        //System.out.println(child.getProtectedValue());
    }
}
