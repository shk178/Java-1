package extends1.ex3;

public class CarMain {
    public static void main(String[] args) {
        ElectricCar electricCar = new ElectricCar();
        electricCar.openDoor();
        electricCar.move();
        electricCar.charge();
        GasCar gasCar = new GasCar();
        gasCar.openDoor();
        gasCar.move();
        gasCar.charge();
        HydrogenCar hydrogenCar = new HydrogenCar();
        hydrogenCar.openDoor();
        hydrogenCar.move();
        hydrogenCar.charge();
    }
}
