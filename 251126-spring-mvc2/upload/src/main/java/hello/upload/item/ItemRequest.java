package hello.upload.item;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 생성/업데이트 요청에서 사용되는 입력 DTO.
 * 사용자가 업로드한 MultipartFile 그대로 받는다.
 */
@Data
public class ItemRequest {

    private Long requestItemId;
    private String requestItemName;

    private MultipartFile uploadedAttachment;        // 단일 첨부파일
    private List<MultipartFile> uploadedImages;      // 여러 이미지 파일

}
