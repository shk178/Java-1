package poly.car;

public class K3 extends Car {
    @Override
    public void start() {
        System.out.println("k3 start");
    }
    @Override
    public void off() {
        System.out.println("off");
    }
    @Override
    public void press() {
        System.out.println("press");
    }
}
