package hello.upload.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 파일 저장을 담당하는 Utility 컴포넌트.
 * - 파일을 서버에 저장
 * - 저장된 파일명을 UUID 기반으로 생성
 * - FileMetadata 생성하여 반환
 */
@Component
public class FileStorage {

    @Value("${file.dir}")
    private String fileDir;

    /**
     * 저장된 파일의 전체 경로를 반환한다.
     */
    public String fullPath(String fileName) {
        return fileDir + fileName;
    }

    /**
     * 여러 MultipartFile을 저장하고 FileMetadata 목록으로 반환.
     */
    public List<FileMetadata> storeFiles(List<MultipartFile> files) throws IOException {
        List<FileMetadata> result = new ArrayList<>();
        if (files == null) return result;

        for (MultipartFile file : files) {
            FileMetadata meta = storeFile(file);
            if (meta != null) {
                result.add(meta);
            }
        }
        return result;
    }

    /**
     * 단일 MultipartFile 저장.
     */
    public FileMetadata storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = file.getOriginalFilename();
        String storedName = createStoredFileName(originalName);

        // 서버에 파일 저장
        file.transferTo(new File(fullPath(storedName)));

        return new FileMetadata(originalName, storedName);
    }

    /**
     * 서버에 저장할 파일명 생성: UUID.확장자
     */
    private String createStoredFileName(String originalName) {
        String ext = extractExt(originalName);
        return UUID.randomUUID().toString() + "." + ext;
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExt(String filename) {
        int pos = filename.lastIndexOf(".");
        return filename.substring(pos + 1);
    }
}
