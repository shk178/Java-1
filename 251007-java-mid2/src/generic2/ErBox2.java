package generic2;

public class ErBox2<T> {
    private Class<T> clazz;
    private T t;
    ErBox2(Class<T> clazz, T t) {
        this.clazz = clazz;
        this.t = t;
    }
    //clazz를 통해 타입 정보를 유지
    boolean check(Object obj) {
        return clazz.isInstance(obj);
    }
    //리플렉션으로 객체를 생성
    T gen() throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
        //java: unreported exception java.lang.NoSuchMethodException; must be caught or declared to be thrown
    }
    T gen2() throws Exception {
        return clazz.getConstructor(clazz).newInstance(t);
        //java: unreported exception java.lang.NoSuchMethodException; must be caught or declared to be thrown
    }
    public static void main(String[] args) throws Exception {
        ErBox2<String> box = new ErBox2<>(String.class, "hello");
        System.out.println(box.check("world")); //true
        System.out.println(box.check(123)); //false
        String generated = box.gen();
        //java: unreported exception java.lang.Exception; must be caught or declared to be thrown
        System.out.println("Generated: [" + generated + "]"); //Generated: []
        //String.class.getDeclaredConstructor().newInstance()는 기본 생성자를 호출
        //String의 기본 생성자는 new String()이고, 이는 빈 문자열을 생성
        //clazz가 Integer.class였다면 newInstance()는 기본 생성자가 없어서 예외가 발생
        String generated2 = box.gen2();
        //java: unreported exception java.lang.Exception; must be caught or declared to be thrown
        System.out.println("Generated2: [" + generated2 + "]"); //Generated2: [hello]
        //Integer.class.getConstructor(Integer.class)는 없어서 예외가 발생
    }
}
