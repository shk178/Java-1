package oop1.ex;
import java.util.Scanner;

public class RectangleOopMain {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("width: ");
        int w = input.nextInt();
        System.out.print("height: ");
        int h = input.nextInt();
        Rectangle rectangle = new Rectangle();
        rectangle.width = w;
        rectangle.height = h;
        System.out.println(rectangle.calculateArea());
        System.out.println(rectangle.calculatePerimeter());
        System.out.println(rectangle.isSquare());
    }
}
