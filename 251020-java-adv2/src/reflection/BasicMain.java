package reflection;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public class BasicMain {
    public static void main(String[] args) throws ClassNotFoundException {
        // 클래스 메타데이터 조회 준비
        Class<BasicData> basicDataClass = BasicData.class;
        System.out.println(basicDataClass); // class reflection.BasicData
        BasicData basicData = new BasicData(); // BasicData.BasicData 실행
        Class<? extends BasicData> basicDataInstance = basicData.getClass();
        // BasicData거나 그 자식 타입인 인스턴스일 수 있어서 <? extends BasicData>로 쓴다.
        /*
         * Parent parent = new Child();
         * 다형성에 의해 부모 타입이 자식 타입을 담을 수 있다.
         * Class<? extends Parent> byInstance = parent.getClass();
         * System.out.println(byInstance); // 자식이 나온다.
         */
        System.out.println(basicDataInstance); // class reflection.BasicData
        String className = "reflection.BasicData";
        Class<?> basicDataString = Class.forName(className); // throws ClassNotFoundException
        // 이건 이름으로는 어떤 클래스든 찾을 수 있어서 그렇다.
        System.out.println(basicDataString); // class reflection.BasicData
        // 클래스 메타데이터 조회
        StringBuilder sb = new StringBuilder();
        sb.append(Arrays.toString(basicDataClass.getConstructors())).append("\n"); // [public reflection.BasicData()]
        sb.append(Arrays.toString(basicDataClass.getFields())).append("\n");
        // [public java.lang.String reflection.BasicData.publicString]
        sb.append(Arrays.toString(basicDataClass.getMethods())).append("\n");
        // [public void reflection.BasicData.call(), public java.lang.String reflection.BasicData.hello(java.lang.String), public boolean java.lang.Object.equals(java.lang.Object), public java.lang.String java.lang.Object.toString(), public native int java.lang.Object.hashCode(), public final native java.lang.Class java.lang.Object.getClass(), public final native void java.lang.Object.notify(), public final native void java.lang.Object.notifyAll(), public final void java.lang.Object.wait(long) throws java.lang.InterruptedException, public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException, public final void java.lang.Object.wait() throws java.lang.InterruptedException]
        sb.append(Arrays.toString(basicDataClass.getInterfaces())).append("\n");
        // []
        sb.append(Modifier.toString(basicDataClass.getModifiers())).append("\n"); // public
        sb.append(basicDataClass.getSuperclass()).append("\n"); // class java.lang.Object
        sb.append(basicDataClass.getCanonicalName()); // reflection.BasicData
        System.out.println(sb.toString());
    }
}
