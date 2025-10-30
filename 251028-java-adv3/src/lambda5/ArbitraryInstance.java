package lambda5;

import java.util.function.Function;
import java.util.function.Supplier;

public class ArbitraryInstance {
    static class Person {
        private String name;
        public Person() {
            this("Unknown");
        }
        public Person(String name) {
            this.name = name;
        }
        public String introduce() {
            return "I am " + name;
        }
    }
    public static void main(String[] args) {
        Person person1 = new Person("Kim");
        Function<Person, String> one = (Person p) -> p.introduce();
        //System.out.println(one.apply());
        System.out.println(one.apply(person1)); // I am Kim
        Function<Person, String> two = Person::introduce;
        //System.out.println(two.apply());
        System.out.println(two.apply(person1)); // I am Kim
        // 생성자 참조
        Function<String, Person> three = name -> new Person(name);
        Function<String, Person> four = Person::new;
        //System.out.println(three.apply().name);
        System.out.println(three.apply("Lee").name); // Lee
        //System.out.println(four.apply().name);
        System.out.println(four.apply("Lee").name); // Lee
        // 생성자 참조2
        Supplier<Person> five = () -> new Person();
        Supplier<Person> six = Person::new;
        System.out.println(five.get().name); // Unknown
        System.out.println(six.get().name); // Unknown
    }
}
