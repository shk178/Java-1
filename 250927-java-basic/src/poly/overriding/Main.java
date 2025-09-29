package poly.overriding;

public class Main {
    public static void main(String[] args) {
        Child child = new Child();
        System.out.println("child.value = " + child.value);
        child.method();
        Parent poly = child;
        System.out.println("poly.value = " + poly.value);
        poly.method();
        Parent poly2 = new GrandChild();
        poly2.method();
        Parent poly3 = new Child2();
        //Child poly4 = new Child2();
        //GrandChild poly5 = (GrandChild)(new Child2());
        GrandChild poly6 = (GrandChild)(new Child());
        //poly6.method();
    }
}
