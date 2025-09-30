package immutable.add;

public class ImmutableAdd {
    private final int value;

    public ImmutableAdd add(int addValue) {
        return new ImmutableAdd(value + addValue);
    }

    public ImmutableAdd(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    @Override
    public String toString() {
        return "ImmutableAdd{" +
                "value=" + value +
                '}';
    }
}
