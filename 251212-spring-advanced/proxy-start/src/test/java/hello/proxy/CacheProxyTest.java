package hello.proxy;

import hello.proxy.pattern1.CacheProxy;
import hello.proxy.pattern1.Client;
import hello.proxy.pattern1.RealSubject;
import hello.proxy.pattern1.Subject;
import org.junit.jupiter.api.Test;

public class CacheProxyTest {
    @Test
    void one() {
        Subject subject = new RealSubject();
        Client client = new Client(subject);
        client.execute();
        client.execute();
        client.execute();
    }
    @Test
    void two() {
        Subject subject = new RealSubject();
        Subject proxy = new CacheProxy(subject);
        Client client = new Client(proxy);
        client.execute();
        client.execute();
        client.execute();
    }
}
/*
RealSubject.operation
RealSubject.operation
RealSubject.operation
CacheProxy.operation
RealSubject.operation
CacheProxy.operation
CacheProxy.operation
 */