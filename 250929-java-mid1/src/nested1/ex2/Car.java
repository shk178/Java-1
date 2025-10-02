package nested1.ex2;

public class Car {
    private String model;
    private int chargeLevel;
    private Engine engine;
    public Car(String model, int chargeLevel) {
        this.model = model;
        this.chargeLevel = chargeLevel;
        this.engine = new Engine();
    }
    public void engineStart() {
        engine.start();
    }
    private class Engine {
        private void start() {
            System.out.println(model);
            System.out.println(chargeLevel);
        }
    }
}
