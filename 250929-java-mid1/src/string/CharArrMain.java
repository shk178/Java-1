package string;

public class CharArrMain {
    public static void main(String[] args) {
        char[] chars = new char[]{'h', 'e', 'l', 'l', 'o'};
        System.out.println(chars); //"hello" 출력
        int[] ints = new int[]{1, 2, 3, 4, 5};
        System.out.println(ints); //.toString() 출력
        String str = "hello";
        System.out.println(str.charAt(0));
        //System.out.println(str.charAt(-1));
        for (char c : str.toCharArray()) {
            System.out.println("c = " + c);
        }
    }
}
