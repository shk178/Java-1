package thread;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public abstract class MyLogger {
    private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public static void log(Object obj) {
        String t = LocalTime.now().format(f);
        System.out.printf("%s [%9s] %s\n", t, Thread.currentThread().getName(), obj);
    }
}
