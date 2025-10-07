package generic2;

public class GenMethod {
    public static Object objMethod(Object obj) {
        System.out.println(obj);
        return obj;
    }
    public static <T> T genMethod(T t) {
        System.out.println(t);
        return t;
    }
}
