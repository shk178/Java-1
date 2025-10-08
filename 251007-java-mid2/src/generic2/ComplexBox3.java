package generic2;
import generic.*;

public class ComplexBox3<T extends Animal> {
    static <Z extends Animal> Z printAndReturn(Z z) {
        System.out.println("z: " + z.getClass());
        z.sound();
        return z;
    }
    public static void main(String[] args) {
        Dog dog = new Dog("누렁이", 100);
        ComplexBox3.printAndReturn(dog);
        //z: class generic.Dog
        //멍
    }
}
