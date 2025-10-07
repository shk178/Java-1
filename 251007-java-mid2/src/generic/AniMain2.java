package generic;

public class AniMain2 {
    public static void main(String[] args) {
        AniHos<Dog> dogHospital = new AniHos<>();
        dogHospital.set(new Dog("멍멍이", 10));
        Animal dog1 = dogHospital.bigger(new Dog("누렁이", 20));
        System.out.println(dog1); //Animal{name='누렁이', size=20}
        AniHos<Cat> catHospital = new AniHos<>();
        catHospital.set(new Cat("야옹이", 5));
        //dogHospital.set(new Cat("야옹이", 5));
        //Animal dog2 = dogHospital.bigger(new Cat("야옹이", 5));
        //java: incompatible types: generic.Cat cannot be converted to generic.Dog
    }
}