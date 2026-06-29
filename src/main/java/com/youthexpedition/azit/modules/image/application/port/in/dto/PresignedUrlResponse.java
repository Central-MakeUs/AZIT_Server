package com.youthexpedition.azit.modules.image.application.port.in.dto;

public record PresignedUrlResponse(
        String presignedUrl,
        String imageUrl
) {
    public static PresignedUrlResponse of(String presignedUrl, String imageUrl) {
        return new PresignedUrlResponse(presignedUrl, imageUrl);
    }
}
