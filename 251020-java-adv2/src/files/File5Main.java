package files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class File5Main {
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
            List<String> lines = Files.readAllLines(path, UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                System.out.println((i + 1) + " : " + lines.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
/*
abc
가나다
1 : abc
2 : 가나다
 */