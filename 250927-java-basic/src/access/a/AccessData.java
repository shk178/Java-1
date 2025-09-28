package access.a;

public class AccessData {
    public int publicField;
    int defaultField;
    private int privateField;
    public void publicMethod() {
        publicField = 1;
        defaultField = 0;
        privateField = 0;
        showField("publicMethod");
    }
    void defaultMethod() {
        defaultField = 1;
        publicField = 0;
        privateField = 0;
        showField("defaultMethod");
    }
    private void privateMethod() {
        privateField = 1;
        publicField = 0;
        defaultField = 0;
        showField("privateMethod");
    }
    private void showField(String method) {
        System.out.println("method 실행: " + method);
        System.out.println(publicField + " " + defaultField + " " + privateField);
    }
}
