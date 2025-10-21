package files;

import java.io.File;
import java.io.IOException;

public class File2Main {
    public static void main(String[] args) {
        // "temp/.." 는 “temp 디렉터리의 상위 폴더”를 의미하는 상대 경로
        File file = new File("temp/..");
        // getPath(): File 객체를 만들 때 입력한 경로(그대로 출력)
        // temp\..
        System.out.println("file.getPath() = " + file.getPath());
        // getAbsolutePath(): 현재 작업 디렉터리를 기준으로 한 절대 경로
        /* C:\Users\\user\Documents\GitHub\Java-1\251020-java-adv2\temp\.. */
        System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
        // getCanonicalPath(): .., . 등을 모두 정리한 실제 물리적 경로 (정규 경로)
        /* C:\Users\\user\Documents\GitHub\Java-1\251020-java-adv2 */
        try {
            System.out.println("file.getCanonicalPath() = " + file.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // file.listFiles(): 해당 디렉터리 안의 모든 파일과 하위 디렉터리 목록을 File[] 배열로 반환
        // file이 가리키는 경로가 디렉터리가 아니라면 null을 반환
        File[] files = file.listFiles();
        for (File f : files) {
            // f.isFile(): 일반 파일이면 true
            // f.isDirectory(): 디렉터리면 true
            // f.getName(): 파일(또는 폴더)의 이름만 출력
            System.out.println((f.isFile() ? "F" : "D") + " | " + f.getName());
        }
    }
}
/*
file.getPath() = temp\..
file.getAbsolutePath() = C:\Users\\user\Documents\GitHub\Java-1\251020-java-adv2\temp\..
file.getCanonicalPath() = C:\Users\\user\Documents\GitHub\Java-1\251020-java-adv2
F | .gitignore
D | .idea
F | 251020-java-adv2.iml
F | Note-1.md
F | Note-2.md
F | Note-3.md
F | Note-4.md
D | out
D | src
D | temp
 */