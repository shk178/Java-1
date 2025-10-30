package stream;

import java.util.NoSuchElementException;

public final class MyOptional<T> {
    private final T value;
    private MyOptional(T value) {
        this.value = value;
    }
    public static <R> MyOptional<R> of(R value) {
        return new MyOptional(value);
    }
    public boolean isPresent() {
        return value != null;
    }
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("value == null");
        } else {
            return value;
        }
    }
}
