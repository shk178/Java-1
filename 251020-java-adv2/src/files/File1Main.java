package files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class File1Main {
    public static void main(String[] args) {
        // Path.of(String first, String... more): 파일 또는 디렉터리 경로를 나타내는 Path 객체 생성
        // Paths.get(String first, String... more): Path.of()과 동일
        Path file = Path.of("temp/example.txt"); // Path.of("temp", "example.txt");도 된다.
        Path dir = Path.of("temp/exampleDir");
        // Files.exists(Path path): 파일 또는 디렉터리가 존재하는지 확인
        System.out.println("Files.exists(file) = " + Files.exists(file));
        // Files.createFile(Path path): 지정한 경로에 새로운 파일을 생성
        try {
            Files.createFile(file);
            System.out.println("File created");
        } catch (IOException e) {
            System.out.println(e); // 이미 같은 이름의 파일이 있으면 FileAlreadyExistsException 발생
        }
        // Files.isRegularFile(Path path): 일반 파일(regular file)인지 확인
        // false: 디렉터리나 존재하지 않음
        System.out.println("Files.isRegularFile(file) = " + Files.isRegularFile(file));
        // Files.isDirectory(Path path): 디렉터리인지 확인
        // false: 디렉터리가 아니거나 존재하지 않음
        System.out.println("Files.isDirectory(dir) = " + Files.isDirectory(dir));
        // Path 객체.getFileName(): 경로 중 마지막 이름(파일명)만 반환
        System.out.println("file.getFileName() = " + file.getFileName());
        // Files.size(Path path): 파일의 크기를 바이트 단위로 반환
        // 반환값: 파일 크기 (long 타입)
        try {
            System.out.println("Files.size(file) = " + Files.size(file));
        } catch (IOException e) {
            System.out.println(e);
        }
        // Files.move(Path source, Path target, CopyOption... options): 파일 이동 또는 이름 변경
        // source: 원본 파일 경로
        // target: 이동할 대상 파일 경로
        // options: 선택적 동작 지정 (가변인자)
        // StandardCopyOption.REPLACE_EXISTING: 대상 파일이 이미 존재하면 덮어씀
        // StandardCopyOption.ATOMIC_MOVE: 이동을 원자적으로 수행 (중단되면 원래 상태 유지)
        Path newFile = Paths.get("temp/newExample.txt");
        try {
            System.out.println("Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING) = " +
                    Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING));
        } catch (IOException e) {
            System.out.println(e);
        }
        // Files.getLastModifiedTime(Path path): 마지막 수정 시간 반환
        // 반환값: FileTime 객체 (예: 2025-10-21T11:25:25.3351176Z)
        try {
            System.out.println("Files.getLastModifiedTime(newFile) = " + Files.getLastModifiedTime(newFile));
        } catch (IOException e) {
            System.out.println(e);
        }
        // Files.readAttributes(Path path, Class<A> type): 파일의 속성(attribute)을 한 번에 읽음
        // BasicFileAttributes를 사용하면 생성 시간, 크기, 유형 등을 얻을 수 있다.
        // path: 속성을 읽을 파일 경로
        // type: 속성의 타입 클래스 (보통 BasicFileAttributes.class)
        // 반환값: BasicFileAttributes 객체
        // creationTime(): 파일 생성 시간
        // lastModifiedTime(): 마지막 수정 시간
        // size(): 파일 크기
        // isDirectory(): 디렉터리 여부
        // isRegularFile(): 일반 파일 여부
        // isSymbolicLink(): 심볼릭 링크 여부
        try {
            BasicFileAttributes attrs = Files.readAttributes(newFile, BasicFileAttributes.class);
            System.out.println("attrs.creationTime() = " + attrs.creationTime());
            System.out.println("attrs.isDirectory() = " + attrs.isDirectory());
            System.out.println("attrs.isRegularFile() = " + attrs.isRegularFile());
            System.out.println("attrs.isSymbolicLink() = " + attrs.isSymbolicLink());
            System.out.println("attrs.size() = " + attrs.size());
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
/*
Files.exists(file) = false
File created
Files.isRegularFile(file) = true
Files.isDirectory(dir) = true
file.getFileName() = example.txt
Files.size(file) = 0
Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING) = temp\newExample.txt
Files.getLastModifiedTime(newFile) = 2025-10-21T11:25:25.3351176Z
attrs.creationTime() = 2025-10-21T11:25:25.3351176Z
attrs.isDirectory() = false
attrs.isRegularFile() = true
attrs.isSymbolicLink() = false
attrs.size() = 0
 */