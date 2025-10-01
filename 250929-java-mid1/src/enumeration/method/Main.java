package enumeration.method;
import enumeration.Grade;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Grade[] values = Grade.values();
        System.out.println(Arrays.toString(values)); //[BASIC, GOLD, DIA]
        Grade d = Grade.valueOf("DIA");
        System.out.println(d); //DIA
        //Grade e = Grade.valueOf("EIA"); //IllegalArgumentException
    }
}
