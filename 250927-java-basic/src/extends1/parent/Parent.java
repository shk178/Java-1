package extends1.parent;

public class Parent {
    public int publicValue = 1;
    protected int protectedValue = 2;
    int defaultValue = 3;
    private int privateValue = 4;
    public int getPublicValue() {
        return publicValue;
    }
    protected int getProtectedValue() {
        return protectedValue;
    }
    int getDefaultValue() {
        return defaultValue;
    }
    private int getPrivateValue() {
        return privateValue;
    }
    public void printParent() {
        System.out.println(getPublicValue());
        System.out.println(getProtectedValue());
        System.out.println(getDefaultValue());
        System.out.println(getPrivateValue());
    }
}
