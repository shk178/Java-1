package immutable;

public class Address {
    private String value;
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return "Address{" +
                "value='" + value + '\'' +
                '}';
    }
}
