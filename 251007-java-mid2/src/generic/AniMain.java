package generic;

public class AniMain {
    public static void main(String[] args) {
        Dog dog = new Dog("강아지", 100);
        Cat cat = new Cat("고양이", 50);
        Box<Dog> dogBox = new Box<>();
        dogBox.set(dog);
        System.out.println(dog); //Animal{name='강아지', size=100}
        System.out.println(dogBox); //generic.Box@30dae81
        System.out.println(dogBox.get()); //Animal{name='강아지', size=100}
        System.out.println(dogBox.get().getClass()); //class generic.Dog
        Box<Cat> catBox = new Box<>();
        catBox.set(cat);
        System.out.println(cat); //Animal{name='고양이', size=50}
        System.out.println(catBox); //generic.Box@1b2c6ec2
        System.out.println(catBox.get()); //Animal{name='고양이', size=50}
        System.out.println(catBox.get().getClass()); //class generic.Cat
        System.out.println(catBox.getClass()); //class generic.Box
        Box<Animal> aniBox = new Box<>();
        aniBox.set(dog);
        System.out.println(aniBox); //generic.Box@4edde6e5
        System.out.println(aniBox.get()); //Animal{name='강아지', size=100}
        System.out.println(aniBox.get().getClass()); //class generic.Dog
        System.out.println(aniBox.getClass()); //class generic.Box
    }
}
