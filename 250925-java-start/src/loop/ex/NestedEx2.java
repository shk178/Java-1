package loop.ex;

public class NestedEx2 {
    public static void main(String[] args) {
        int land = 9;
        int max = (land + 1) / 2;
        for (int row = 1; row <= max; row++) {
            for (int i = 1; i <= (max - row); i++) {
                System.out.print(" ");
            }
            for (int j = 1; j <= ((row * 2) - 1); j++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}
