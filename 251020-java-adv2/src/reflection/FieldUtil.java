package reflection;

import java.lang.reflect.Field;

public class FieldUtil {
    public static void nullFiledToDefault(Object target) throws IllegalAccessException {
        Class<?> aClass = target.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (field.get(target) != null) {
                continue;
            }
            if (field.getType() == String.class) {
                field.set(target, "");
            } else if (field.getType() == Integer.class) {
                field.set(target, 0);
            }
        }
    }
}
// String s1 = null; // 아무것도 없음
// String s2 = ""; // 비어 있는 문자열 객체 (메모리에 있다.)
// System.out.println(s1.length()); // NullPointerException
// System.out.println(s2.length()); // 0 출력
// int a = 0; // 항상 값이 있어야 함
// Integer b = null; // 객체 참조이므로 null 가능
// Integer num = null; int val = num; // NullPointerException (자동 언박싱)
