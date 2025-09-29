package object;

public class Car {
    public void move() {
        System.out.println("부릉");
    }
    @Override
    public String toString() {
        return "Car " + System.identityHashCode(this);
    }
}
