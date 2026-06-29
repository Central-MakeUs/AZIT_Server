package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.modules.member.domain.model.enums.TermsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TermsVersion {
    private final Long id;
    private final TermsType termsType;
    private final String version;
    private final boolean isRequired;
    private final LocalDateTime effectiveAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
