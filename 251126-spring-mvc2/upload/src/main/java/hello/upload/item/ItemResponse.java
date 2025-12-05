package hello.upload.item;

import lombok.Data;
import java.util.List;

/**
 * 화면 또는 API 응답에서 사용하는 출력 DTO.
 * 사용자가 보기 쉬운 구조로 파일명/URL 제공.
 */
@Data
public class ItemResponse {

    private Long responseItemId;
    private String responseItemName;

    // 첨부파일 정보
    private String attachmentOriginalName;
    private String attachmentDownloadUrl;

    // 이미지 파일 정보
    private List<String> imageOriginalNames;
    private List<String> imageUrls;

}
