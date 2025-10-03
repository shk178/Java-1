package nested2.anon2;

public class Main {
    public static void main(String[] args) {
        Animal dog = new Animal() {
            @Override
            void sound() {
                System.out.println("멍멍!");
            }
        };
        dog.sound(); //멍멍!
    }
}
