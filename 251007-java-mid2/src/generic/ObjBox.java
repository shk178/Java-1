package generic;

public class ObjBox {
    private Object value;
    public void set(Object obj) {
        this.value = obj;
    }
    public Object get() {
        return value;
    }
    public static void main(String[] args) {
        ObjBox obj1 = new ObjBox();
        obj1.set(10); //int -> Integer로 오토 박싱
        Object getobj1 = obj1.get();
        System.out.println(getobj1.getClass()); //class java.lang.Integer
        //Integer one = getobj1; //java: incompatible types: java.lang.Object cannot be converted to java.lang.Integer
        Integer one = (Integer) getobj1; //단축키 Inline Variable (Ctrl+Alt+N) 하면 getobj1 -> obj1.get()으로 치환
        obj1.set("100"); //숫자 100 쓰려고 했는데, 문자열로 "100" 썼을 때
        Integer two = (Integer) obj1.get(); //Exception in thread "main" java.lang.ClassCastException: 런타임 오류 발생
    }
}
