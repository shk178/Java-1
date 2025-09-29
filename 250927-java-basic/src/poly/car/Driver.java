package poly.car;

public class Driver {
    private Car car;
    public void setCar(Car car) {
        this.car = car;
    }
    public void drive() {
        car.start();
        car.press();
        car.off();
    }
}
