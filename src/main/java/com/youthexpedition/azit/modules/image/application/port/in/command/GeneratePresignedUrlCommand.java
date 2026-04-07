package com.youthexpedition.azit.modules.image.application.port.in.command;

import com.youthexpedition.azit.modules.image.domain.model.enums.ImageUploadType;

public record GeneratePresignedUrlCommand(
        ImageUploadType type,
        String fileName,
        Long memberId
) {
    public static GeneratePresignedUrlCommand of(ImageUploadType type, String fileName, Long memberId) {
        return new GeneratePresignedUrlCommand(type, fileName, memberId);
    }
}
