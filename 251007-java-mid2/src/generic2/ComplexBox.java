package generic2;
import generic.*;

public class ComplexBox<T extends Animal> {
    private T animal;
    public void set(T animal) {
        this.animal = animal;
    }
    public <Z> Z printAndReturn(Z z) {
        System.out.println("animal: " + animal.getClass());
        System.out.println("z: " + z.getClass());
        return z;
    }
    public static void main(String[] args) {
        Dog dog = new Dog("멍멍이", 100);
        Cat cat = new Cat("야옹이", 50);
        ComplexBox<Dog> box = new ComplexBox<>();
        box.set(dog);
        Animal result = box.printAndReturn(cat);
        //animal: class generic.Dog
        //z: class generic.Cat
        System.out.println(result); //Animal{name='야옹이', size=50}
        System.out.println(result.getClass()); //class generic.Cat
        Cat result2 = box.printAndReturn(cat);
        //animal: class generic.Dog
        //z: class generic.Cat
        System.out.println(result2); //Animal{name='야옹이', size=50}
        System.out.println(result2.getClass()); //class generic.Cat
    }
}
