package reflection;

public class BasicData {
    public String publicString;
    private int privateInt;
    public BasicData() {
        System.out.println("BasicData.BasicData 실행");
    }
    private BasicData(String data) {
        System.out.println("BasicData.BasicData(" + data + ") 실행");
    }
    public void call() {
        System.out.println("BasicData.call 실행");
    }
    public String hello(String str) {
        System.out.println("BasicData.hello(" + str + ") 실행");
        return "hello " + str;
    }
    private void privateMethod() {
        System.out.println("BasicData.privateMethod 실행");
    }
    void defaultMethod() {
        System.out.println("BasicData.package-privateMethod 실행");
    }
    protected void protectedMethod() {
        System.out.println("BasicData.protectedMethod 실행");
    }
}
