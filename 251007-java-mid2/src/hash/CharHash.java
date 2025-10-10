package hash;

public class CharHash {
    static void hashCode(String str) {
        char[] charArr = str.toCharArray();
        System.out.print("charArr=");
        System.out.println(charArr); //a
        System.out.println(charArr.toString()); //[C@b4c966a
        int sum = 0;
        for (char c : charArr) {
            sum += (int) c;
        }
        System.out.println("hashcode=" + sum); //97
    }
    public static void main(String[] args) {
        hashCode("a"); //97
        hashCode("b"); //98
        hashCode("ab"); //195
        hashCode("AD"); //133
        hashCode("BC"); //133
    }
}
