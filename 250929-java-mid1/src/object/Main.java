package object;

public class Main {
    public static void main(String[] args) {
        System.out.println("hello java");
        Child child = new Child();
        Child child2 = new Child();
        Parent poly = child;
        System.out.println(child.toString());
        System.out.println(child.equals(child2));
        System.out.println(child.equals(poly));
        System.out.println(child.hashCode());
        System.out.println(child.getClass());
    }
}
