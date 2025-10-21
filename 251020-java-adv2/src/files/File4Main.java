package files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

public class File4Main {
    private static final String FILE_PATH = "temp/hello2.txt";
    public static void main(String[] args) {
        String writeString = "abc\n가나다";
        System.out.println(writeString);
        Path path = Path.of(FILE_PATH);
        try {
            Files.writeString(path, writeString, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            String readString = Files.readString(path, UTF_8);
            System.out.println(readString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
/*
abc
가나다
abc
가나다
 */