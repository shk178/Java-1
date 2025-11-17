package basic.lecture2;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetterSetter {
    private String name;
    private int age;
    public static void main(String[] args) {
        GetterSetter helloLombok = new GetterSetter();
        helloLombok.setName("name1");
        helloLombok.setAge(11);
        System.out.println(helloLombok.getName());
        System.out.println(helloLombok.getAge());
        System.out.println(helloLombok); // GetterSetter(name=name1, age=11)
    }
}
