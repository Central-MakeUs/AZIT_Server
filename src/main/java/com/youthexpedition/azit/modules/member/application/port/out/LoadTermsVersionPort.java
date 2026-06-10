package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.TermsVersion;

import java.util.List;
import java.util.Set;

public interface LoadTermsVersionPort {
    List<TermsVersion> findAllLatest();
    Set<Long> findConsentedVersionIdsByMemberId(Long memberId);
}
