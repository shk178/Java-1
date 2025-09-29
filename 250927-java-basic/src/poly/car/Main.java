package poly.car;

public class Main {
    public static void main(String[] args) {
        Driver driver = new Driver();
        Car k3 = new K3();
        //new Car();
        driver.setCar(k3);
        driver.drive();
    }
}
