package hello.proxy.pattern2;

public class RealComponent implements Component {
    @Override
    public String operation() {
        System.out.println("RealComponent.operation");
        return "data";
    }
}
