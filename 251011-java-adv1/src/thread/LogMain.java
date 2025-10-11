package thread;
//import static thread.MyLogger.log;를 쓰면 log("hello thread");로 호출 가능

public class LogMain {
    public static void main(String[] args) {
        MyLogger.log("hello thread");
        MyLogger.log(123);
    }
}
//21:14:56.075 [     main] hello thread
//21:14:56.077 [     main] 123