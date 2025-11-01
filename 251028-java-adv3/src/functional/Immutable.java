package functional;

public class Immutable {
    static class Person {
        final String name;
        final int age;
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        //원본 유지됨, 객체는 항상 불변
        public Person withAge(int newAge) {
            return new Person(name, newAge);
        }
    }
}
