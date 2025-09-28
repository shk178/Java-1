package extends1.ex2;

public class CarMain {
    public static void main(String[] args) {
        ElectricCar electricCar = new ElectricCar();
        electricCar.board();
        electricCar.move();
        electricCar.charge();
        GasCar gasCar = new GasCar();
        gasCar.board();
        gasCar.move();
        gasCar.fill();
    }
}
