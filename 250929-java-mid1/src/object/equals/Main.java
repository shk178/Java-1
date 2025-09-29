package object.equals;

public class Main {
    public static void main(String[] args) {
        Person p1 = new Person("이름", 10);
        Person p2 = new Person("이름", 10);
        System.out.println(p1 == p2);
        System.out.println(p1.equals(p2));
    }
}
