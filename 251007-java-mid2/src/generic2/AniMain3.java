package generic2;
import generic.*;

public class AniMain3 {
    public static void main(String[] args) {
        Dog dog = new Dog("멍멍이", 100);
        Cat cat = new Cat("야옹이", 100);
        AnimalMethod.checkUp(dog);
        //멍멍이
        //100
        //멍
        AnimalMethod.checkUp(cat);
        //야옹이
        //100
        //냥
        Dog dog2 = new Dog("누렁이", 200);
        Dog result = AnimalMethod.bigger(dog, dog2);
        System.out.println(result); //Animal{name='누렁이', size=200}
    }
}
