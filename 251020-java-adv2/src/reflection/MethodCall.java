package reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodCall {
    public static void main(String[] args) throws Exception {
        BasicData basicData = new BasicData(); // BasicData.BasicData 실행
        Class<? extends BasicData> clazz = basicData.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println(Arrays.toString(methods));
        // [public java.lang.String reflection.BasicData.hello(java.lang.String), private void reflection.BasicData.privateMethod(), protected void reflection.BasicData.protectedMethod(), void reflection.BasicData.defaultMethod(), public void reflection.BasicData.call()]
        for (Method method : methods) {
            // 접근 허용 (private, protected 등)
            method.setAccessible(true);
            // 파라미터 확인
            if (method.getParameterCount() == 0) {
                System.out.println(">> Invoking: " + method.getName());
                method.invoke(basicData);
            } else {
                System.out.println(">> Skipping: " + method.getName() + " (needs parameters)");
            }
        }
    }
}
/*
>> Skipping: hello (needs parameters)
>> Invoking: privateMethod
BasicData.privateMethod 실행
>> Invoking: protectedMethod
BasicData.protectedMethod 실행
>> Invoking: defaultMethod
BasicData.package-privateMethod 실행
>> Invoking: call
BasicData.call 실행
 */