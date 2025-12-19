package hello.proxy;

import hello.proxy.pattern2.*;
import org.junit.jupiter.api.Test;

public class MsgDecoratorTest {
    @Test
    void one() {
        System.out.println("one");
        Component component = new RealComponent();
        Client2 client2 = new Client2(component);
        client2.execute();
        System.out.println();
    }
    @Test
    void two() {
        System.out.println("two");
        Component component = new RealComponent();
        Component decorator = new MsgDecorator(component);
        Client2 client2 = new Client2(decorator);
        client2.execute();
        System.out.println();
    }
    @Test
    void time() {
        System.out.println("time");
        Component component = new RealComponent();
        Component decorator = new MsgDecorator(component);
        Component time = new TimeDecorator(decorator);
        Client2 client2 = new Client2(time);
        client2.execute();
        System.out.println();
    }
}
/*
one
RealComponent.operation
Client2.execute result(component.operation()) = data

two
MsgDecorator.operation
RealComponent.operation
Client2.execute result(component.operation()) = *data*

time
TimeDecorator.operation
MsgDecorator.operation
RealComponent.operation
duration = 0
Client2.execute result(component.operation()) = *data*
 */