package wrapper;

public class Main {
    public static void main(String[] args) {
        int value = 10;
        System.out.println(compareTo(value, value + 5));
        System.out.println(compareTo(value, value - 5));
        System.out.println(compareTo(value, value + 5 - 5));
    }
    public static int compareTo(int value, int target) {
        if (value < target) return -1;
        else if (target < value) return 1;
        else return 0;
    }
}
