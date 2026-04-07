package com.youthexpedition.azit.modules.image.domain.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageUploadType {
    MEMBER_PROFILE("profile"),
    CREW_IMAGE("crew"),
    STORE_REVIEW("review"),
    ;

    public static final String TEMP_PREFIX = "temp/";

    private final String directory;

    public String buildPath(Long memberId) {
        return directory + "/" + memberId;
    }

    /**
     * temp S3 Key에서 memberId 추출
     * 경로 구조: temp/{directory}/{memberId}/{fileName}
     */
    public static Long extractMemberIdFromTempKey(String tempS3Key) {
        try {
            String[] parts = tempS3Key.split("/");
            return Long.parseLong(parts[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
