package extends1.access;

public class AccessMain {
    public static void main(String[] args) {
        Child2 child = new Child2();
        child.call();
        child.printParent();
        System.out.println(child.getPublicValue());
        System.out.println(child.getProtectedValue());
        System.out.println(child.getDefaultValue());
        //System.out.println(child.getPrivateValue());
    }
}
