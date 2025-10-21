package files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class File3Main {
    public static void main(String[] args) {
        Path path = Path.of("temp/.."); // 상대 경로 지정
        System.out.println("path = " + path); // Path.of()에 전달한 상대 경로
        System.out.println("path.toAbsolutePath() = " + path.toAbsolutePath()); // 현재 실행 위치를 기준으로 한 절대 경로
        try {
            System.out.println("path.toRealPath() = " + path.toRealPath()); // 실제 파일 시스템에서 확인한 실제 경로 (.., . 제거)
        } catch (IOException e) {
            // 존재하지 않는 경로면 IOException 발생
            throw new RuntimeException(e);
        }
        // Files.list(Path dir)는 해당 경로가 디렉터리일 때
        // 그 안의 파일 및 하위 폴더들을 Stream<Path> 형태로 반환
        // Stream이므로 forEach, filter, map 같은 연산을 사용할 수 있음
        // 한 번만 사용 가능 (close 필요)
        try {
            Stream<Path> pathStream = Files.list(path); // 스트림 형태로 파일 목록 가져오기
            List<Path> list = pathStream.toList(); // 스트림을 리스트로 변환
            pathStream.close();
            for (Path p : list) {
                // 파일인지 디렉터리인지 구분하고 이름 출력
                System.out.println((Files.isRegularFile(p) ? "F" : "D") + " | " + p.getFileName());
            }
        } catch (IOException e) {
            // 디렉터리가 존재하지 않으면 IOException 발생
            throw new RuntimeException(e);
        }
    }
}
/*
path = temp\..
path.toAbsolutePath() = C:\Users\\user\Documents\GitHub\Java-1\251020-java-adv2\temp\..
path.toRealPath() = C:\Users\\user\Documents\GitHub\Java-1\251020-java-adv2
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