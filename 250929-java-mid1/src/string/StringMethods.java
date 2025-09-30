package string;

public class StringMethods {
    public static void main(String[] args) {
        System.out.println("a".compareTo("b")); //-1
        System.out.println("b".compareTo("a")); //1
        System.out.println("a".compareTo("a")); //0
        System.out.println("c".compareTo("a")); //2
        System.out.println("aa".compareTo("bb")); //-1
        System.out.println("ab".compareTo("aa")); //1
        System.out.println("ba".compareTo("aa")); //1
        System.out.println("cb".compareTo("aa")); //2
        System.out.println("bc".compareTo("aa")); //1
        System.out.println("a".compareTo("aa")); //-1
        System.out.println("aaa".compareTo("a")); //2
        System.out.println("a".compareTo("aab")); //-2
        System.out.println("b".compareTo("aab")); //1
        System.out.println("b".compareTo("bab")); //-2
        System.out.println("a".compareTo("baa")); //-1
        System.out.println("c".compareTo("caa")); //-2
    }
}
