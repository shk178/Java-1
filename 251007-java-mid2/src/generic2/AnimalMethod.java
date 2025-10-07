package generic2;
import generic.*;

public class AnimalMethod {
    public static <T extends Animal> void checkUp(T t) {
        System.out.println(t.getName());
        System.out.println(t.getSize());
        t.sound();
    }
    public static <T extends Animal> T bigger(T t1, T t2) {
        return t1.getSize() > t2.getSize() ? t1 : t2;
    }
}
