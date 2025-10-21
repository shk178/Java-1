package files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class File8Main {
    public static void main(String[] args) throws IOException {
        long sTime = System.currentTimeMillis();
        Path source = Path.of("temp/copy.dat");
        Path target = Path.of("temp/copy_new.dat");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        long eTime = System.currentTimeMillis();
        System.out.println("(eTime - sTime) = " + (eTime - sTime)); // (eTime - sTime) = 95
    }
}
