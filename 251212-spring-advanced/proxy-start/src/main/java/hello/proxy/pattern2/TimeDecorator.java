package hello.proxy.pattern2;

public class TimeDecorator implements Component {
    private Component component;
    public TimeDecorator(Component component) {
        this.component = component;
    }
    @Override
    public String operation() {
        System.out.println("TimeDecorator.operation");
        long sTime = System.currentTimeMillis();
        String result = component.operation();
        long eTime = System.currentTimeMillis();
        System.out.println("duration = " + (eTime - sTime));
        return result;
    }
}
