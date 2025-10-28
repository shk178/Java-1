package annotation;

import java.lang.reflect.Method;

public class Main1 {
    public static void main(String[] args) {
        TestController testController = new TestController();
        Class<? extends TestController> aClass = testController.getClass();
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            SimpleMapping simpleMapping = method.getAnnotation(SimpleMapping.class);
            if (simpleMapping != null) {
                System.out.println(simpleMapping.value());
                // /site2
                // /site1
                // /
            }
        }
    }
}
