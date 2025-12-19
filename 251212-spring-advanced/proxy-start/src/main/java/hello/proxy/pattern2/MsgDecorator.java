package hello.proxy.pattern2;

public class MsgDecorator implements Component {
    private Component component;
    public MsgDecorator(Component component) {
        this.component = component;
    }
    @Override
    public String operation() {
        System.out.println("MsgDecorator.operation");
        String result = component.operation();
        String decoResult = "*" + result + "*";
        return decoResult;
    }
}
