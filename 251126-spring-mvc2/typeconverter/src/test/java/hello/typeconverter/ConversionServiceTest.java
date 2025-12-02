package hello.typeconverter;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import static org.assertj.core.api.Assertions.assertThat;

public class ConversionServiceTest {
    @Test
    void test() {
        DefaultConversionService cs = new DefaultConversionService();
        cs.addConverter(new CustomConverter());
        One one = new One();
        one.setOneInt(1);
        one.setOneInteger(10);
        one.setOneString("100");
        Two two = cs.convert(one, Two.class);
        System.out.println(two); // Two(twoInt=2, twoInteger=20, twoString=100100)
    }
}
