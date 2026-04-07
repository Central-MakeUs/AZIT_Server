package com.youthexpedition.azit.modules.image.domain.model.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageUploadType {
    MEMBER_PROFILE("profile"),
    CREW_IMAGE("crew"),
    STORE_REVIEW("review"),
    ;

    private final String directory;

    public String buildPath(Long memberId) {
        return directory + "/" + memberId;
    }
}
