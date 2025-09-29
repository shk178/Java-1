package poly.ex1;

public class Main {
    public static void main(String[] args) {
        Animal dog = new Dog();
        Animal cat = new Cat();
        Animal cow = new Cow();
        sound(dog);
        sound(cat);
        sound(cow);
    }
    private static void sound(Animal name) {
        System.out.println("start");
        name.sound();
        System.out.println("end");
    }
}
