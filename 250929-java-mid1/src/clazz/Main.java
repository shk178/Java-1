package clazz;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class clazz1 = String.class; //클래스명.class로 조회
        Class clazz2 = new String().getClass(); //객체로 조회
        Class clazz3 = Class.forName("java.lang.String"); //문자열로 조회
        System.out.println(clazz1.getDeclaredFields()); //[Ljava.lang.reflect.Field;@b4c966a
        Field[] fields = clazz1.getDeclaredFields();
        for (Field field : fields) {
            System.out.println("field = " + field);
        }
        //field = private final byte[] java.lang.String.value
        //field = private final byte java.lang.String.coder
        //field = private int java.lang.String.hash
        //field = private boolean java.lang.String.hashIsZero
        //field = private static final long java.lang.String.serialVersionUID
        //field = static final boolean java.lang.String.COMPACT_STRINGS
        //field = private static final java.io.ObjectStreamField[] java.lang.String.serialPersistentFields
        //field = private static final char java.lang.String.REPL
        //field = public static final java.util.Comparator java.lang.String.CASE_INSENSITIVE_ORDER
        //field = static final byte java.lang.String.LATIN1
        //field = static final byte java.lang.String.UTF16
        Method[] methods = clazz1.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println("method = " + method);
        }
        //method = byte[] java.lang.String.value()
        //method = public boolean java.lang.String.equals(java.lang.Object)
        //method = public int java.lang.String.length()
        //method = public java.lang.String java.lang.String.toString()
        //method = static void java.lang.String.checkIndex(int,int)
        //method = public int java.lang.String.hashCode()
        //...
        Constructor[] constructors = clazz1.getConstructors();
        for (Constructor constructor : constructors) {
            System.out.println("constructor = " + constructor);
        }
        //...
        //constructor = public java.lang.String(byte[])
        //constructor = public java.lang.String(java.lang.StringBuffer)
        //constructor = public java.lang.String(char[],int,int)
        //constructor = public java.lang.String(char[])
        //constructor = public java.lang.String(java.lang.String)
        //constructor = public java.lang.String()
        //...
        Constructor<?> constructor2 = clazz1.getDeclaredConstructor();
        System.out.println("constructor2 = " + constructor2);
        //constructor2 = public java.lang.String()
        Class<?> clazz4 = clazz1.getSuperclass();
        System.out.println(clazz4);
        //class java.lang.Object
        Class<?>[] clazz5 = clazz1.getInterfaces();
        for (Class<?> aClass : clazz5) {
            System.out.println("aClass = " + aClass);
        }
        //aClass = interface java.io.Serializable
        //aClass = interface java.lang.Comparable
        //aClass = interface java.lang.CharSequence
        //aClass = interface java.lang.constant.Constable
        //aClass = interface java.lang.constant.ConstantDesc
        Class<?> unknownClass = Class.forName("java.lang.String");
        String s = (String) unknownClass.getDeclaredConstructor().newInstance();
    }
}
