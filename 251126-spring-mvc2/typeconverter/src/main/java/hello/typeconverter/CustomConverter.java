package hello.typeconverter;

import org.springframework.core.convert.converter.Converter;

public class CustomConverter implements Converter<One, Two> {
    @Override
    public Two convert(One one) {
        Two two = new Two();
        two.setTwoInt(one.getOneInt() * 2);
        two.setTwoInteger(one.getOneInteger() * 2);
        two.setTwoString(one.getOneString() + one.getOneString());
        return two;
    }
}
