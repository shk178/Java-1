package hello.proxy.pattern2;

public class Client2 {
    private Component component;
    public Client2(Component component) {
        this.component = component;
    }
    public void execute() {
        String result = component.operation();
        System.out.println("Client2.execute result(component.operation()) = " + result);
    }
}
