package com.youthexpedition.azit.modules.image.application.service;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.image.application.port.in.ImageUseCase;
import com.youthexpedition.azit.modules.image.application.port.in.command.GeneratePresignedUrlCommand;
import com.youthexpedition.azit.modules.image.application.port.in.dto.PresignedUrlResponse;
import com.youthexpedition.azit.modules.image.application.port.out.ImageStoragePort;
import com.youthexpedition.azit.modules.image.domain.model.enums.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService implements ImageUseCase {

    private final ImageStoragePort imageStoragePort;
    private final ImageUrlFormatUtil imageUrlFormatUtil;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofMinutes(5);

    @Override
    public PresignedUrlResponse generatePresignedUrl(GeneratePresignedUrlCommand command) {
        String extension = extractExtension(command.fileName());
        validateExtension(extension);

        String s3Key = buildS3Key(command, extension);
        String contentType = resolveContentType(extension);

        String presignedUrl = imageStoragePort.generatePresignedUrl(s3Key, contentType, PRESIGNED_URL_EXPIRY);
        String imageUrl = imageUrlFormatUtil.buildFullImageUrl("/" + s3Key);

        return PresignedUrlResponse.of(presignedUrl, imageUrl);
    }

    private String buildS3Key(GeneratePresignedUrlCommand command, String extension) {
        String fileName = LocalDate.now() + "_" + UUID.randomUUID();
        return "temp/" + command.type().buildPath(command.memberId()) + "/" + fileName + "." + extension;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ImageErrorCode.INVALID_FILE_NAME);
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (extension.isBlank()) {
            throw new BusinessException(ImageErrorCode.INVALID_FILE_NAME);
        }
        return extension.toLowerCase();
    }

    private void validateExtension(String extension) {
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ImageErrorCode.UNSUPPORTED_FILE_EXTENSION);
        }
    }

    private String resolveContentType(String extension) {
        return "image/" + (extension.equals("jpg") ? "jpeg" : extension);
    }
}
