package lambda3;

@FunctionalInterface
public interface GenericFunction<T, R> {
    R apply(T t);
}
