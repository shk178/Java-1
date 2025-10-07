package generic;

public class Box<T> {
    private T value;
    public void set(T t) {
        this.value = t;
    }
    public T get() {
        return value;
    }
}
