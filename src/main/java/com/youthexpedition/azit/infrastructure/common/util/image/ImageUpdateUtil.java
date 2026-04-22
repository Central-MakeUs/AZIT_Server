package com.youthexpedition.azit.infrastructure.common.util.image;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.image.application.port.out.ImageStoragePort;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageErrorCode;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageUploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ImageUpdateUtil {

    public static final String DEFAULT_S3_PREFIX = "default/";
    public static final String DEFAULT_SLASH = "/";

    private final ImageUrlFormatUtil imageUrlFormatUtil;
    private final ImageStoragePort imageStoragePort;

    /**
     * @param incomingUrl     요청으로 받은 이미지 URL
     * @param currentUrl      현재 저장된 이미지 URL
     * @param entityId        소유권 검증에 사용할 엔티티 ID (memberId 또는 crewId 등)
     * @param allowExternalUrl 외부 URL(소셜 로그인 프로필 등) 유지 허용 여부
     * @param updateUrl       변경된 최종 URL을 엔티티에 반영하는 콜백
     */
    public void update(String incomingUrl, String currentUrl, Long entityId, boolean allowExternalUrl, Consumer<String> updateUrl) {
        String incomingS3Key = imageUrlFormatUtil.extractS3Key(incomingUrl);
        String currentS3Key = imageUrlFormatUtil.extractS3Key(currentUrl);

        // S3 키 기준으로 변경 여부 판단
        boolean imageChanged = !Objects.equals(incomingS3Key, currentS3Key) // S3 경로
                || (allowExternalUrl && incomingS3Key == null && !Objects.equals(incomingUrl, currentUrl)); // 상대 경로

        if (!imageChanged) return;

        if (incomingS3Key == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
        }

        // 새 커스텀 이미지: temp 파일 존재 여부 및 crewId 소유권 검증
        if (incomingS3Key.startsWith(ImageUploadType.TEMP_PREFIX)) {
            if (!imageStoragePort.exists(incomingS3Key)) {
                throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
            }
            Long pathEntityId = ImageUploadType.extractEntityIdFromTempKey(incomingS3Key);
            if (pathEntityId == null) {
                throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
            }
            if (!entityId.equals(pathEntityId)) {
                throw new BusinessException(ImageErrorCode.IMAGE_OWNERSHIP_MISMATCH);
            }
            // 기존 커스텀 이미지 S3 삭제 (기본 이미지 제외)
            deleteOldCustomImage(currentS3Key);

            // temp → 실제 경로로 이동 후 상대 경로 저장
            String finalS3Key = incomingS3Key.substring(ImageUploadType.TEMP_PREFIX.length());
            imageStoragePort.move(incomingS3Key, finalS3Key);
            updateUrl.accept(DEFAULT_SLASH + finalS3Key);

        } else if (incomingS3Key.startsWith(DEFAULT_S3_PREFIX)) {
            // 기본 이미지
            deleteOldCustomImage(currentS3Key);
            updateUrl.accept(DEFAULT_SLASH + incomingS3Key);

        } else {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_UPLOADED);
        }
    }

    private void deleteOldCustomImage(String oldS3Key) {
        if (oldS3Key != null && !oldS3Key.startsWith(DEFAULT_S3_PREFIX)) {
            imageStoragePort.delete(oldS3Key);
        }
    }
}
