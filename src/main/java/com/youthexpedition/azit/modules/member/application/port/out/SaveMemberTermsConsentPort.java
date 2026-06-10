package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.MemberTermsConsent;
import com.youthexpedition.azit.modules.member.domain.model.MemberTermsConsentHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SaveMemberTermsConsentPort {
    void saveAll(List<MemberTermsConsent> consents);
    void updateAgreedAt(Long memberId, Set<Long> versionIds, LocalDateTime agreedAt);
    void deleteByMemberIdAndVersionIds(Long memberId, Set<Long> versionIds);
    void saveAllHistory(List<MemberTermsConsentHistory> histories);
}
