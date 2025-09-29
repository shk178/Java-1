package object;

public class Main2 {
    public static void main(String[] args) {
        Car car = new Car();
        Dog dog = new Dog();
        methodRun(dog);
        methodRun(car);
        Object[] objects = {car, dog};
        size(objects);
        System.out.println(car);
        Object carobj = new Car();
        System.out.println(carobj);
        System.out.println(carobj.hashCode());
    }
    public static void methodRun(Object obj) {
        //obj.move();
        //obj.sound();
        if (obj instanceof Dog dog) {
            dog.sound();
        } else if (obj instanceof Car car) {
            car.move();
        }
    }
    private static void size(Object[] objects) {
        System.out.println("objects.length = " + objects.length);
    }
}
