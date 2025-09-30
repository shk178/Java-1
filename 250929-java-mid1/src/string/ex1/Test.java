package string.ex1;

public class Test {
    public static void main(String[] args) {
        String[] arr = {"hello", "java", "spring"};
        int sum = 0;
        for (String s : arr) {
            int len = s.length();
            System.out.println(s + ": " + len);
            sum += len;
        }
        System.out.println("sum = " + sum);
        String str = "hello.txt";
        String ext = ".txt";
        int i = str.indexOf(ext);
        System.out.println(str.substring(0, i));
        System.out.println(str.substring(i));
        String str2 = "start hello java, hello spring, hello";
        String key = "hello";
        int count = 0;
        while (true) {
            if (str2.contains(key)) {
                count++;
                str2 = str2.substring(str2.indexOf(key) + key.length());
                System.out.println(str2);
            } else {
                break;
            }
        }
        System.out.println("count = " + count);
        String str3 = "hello java. spring jpa java";
        System.out.println(str3.replace("java", "jvm"));
        String email = "hello@example.com";
        String[] arr2 = email.split("@");
        for (String s : arr2) {
            System.out.println("s = " + s);
        }
        String result = String.join("@", arr2);
        System.out.println("result = " + result);
    }
}
