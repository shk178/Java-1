package basic.lecture3;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class NetworkClient3 {
    private String url;
    public NetworkClient3() {
        System.out.println("생성자 호출, url = " + url);
    }
    // 객체 생성 후 호출
    public void setUrl(String url) {
        this.url = url;
    }
    // 서비스 시작 시 호출
    public void connect() {
        System.out.println("connect: " + url);
    }
    public void call(String message) {
        System.out.println("call: " + url + ", message = " + message);
    }
    // 서비스 종료 시 호출
    public void disconnect() {
        System.out.println("disconnect: " + url);
    }
    @PostConstruct
    public void init() {
        connect();
        call("초기화 연결 메시지");
    }
    @PreDestroy
    public void close() {
        disconnect();
    }
}
