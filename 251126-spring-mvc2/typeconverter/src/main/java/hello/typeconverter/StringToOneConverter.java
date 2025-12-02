package hello.typeconverter;

import org.springframework.core.convert.converter.Converter;

public class StringToOneConverter implements Converter<String, One> {

    @Override
    public One convert(String source) {
        One one = new One();

        String[] parts = source.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "oneInt" -> one.setOneInt(Integer.parseInt(value));
                case "oneInteger" -> one.setOneInteger(Integer.parseInt(value));
                case "oneString" -> one.setOneString(value);
            }
        }
        return one;
    }
}
