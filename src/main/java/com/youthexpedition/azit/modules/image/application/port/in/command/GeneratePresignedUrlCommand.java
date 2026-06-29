package com.youthexpedition.azit.modules.image.application.port.in.command;

import com.youthexpedition.azit.modules.image.domain.model.enums.ImageUploadType;

public record GeneratePresignedUrlCommand(
        ImageUploadType type,
        String fileName,
        Long memberId,
        Long crewId
) {
    public static GeneratePresignedUrlCommand of(ImageUploadType type, String fileName, Long memberId, Long crewId) {
        return new GeneratePresignedUrlCommand(type, fileName, memberId, crewId);
    }
}
