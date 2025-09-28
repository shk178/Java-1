package access.a;

public class AccessMain1 {
    public static void main(String[] args) {
        AccessData data = new AccessData();
        System.out.println("data.publicField = " + data.publicField);
        System.out.println("data.defaultField = " + data.defaultField);
        //System.out.println("data.privateField = " + data.privateField);
        data.publicMethod();
        data.defaultMethod();
        //data.privateMethod();
    }
}
