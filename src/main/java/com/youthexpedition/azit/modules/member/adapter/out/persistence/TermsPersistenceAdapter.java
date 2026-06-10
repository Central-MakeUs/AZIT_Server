package com.youthexpedition.azit.modules.member.adapter.out.persistence;

import com.youthexpedition.azit.modules.member.adapter.out.mapper.MemberTermsConsentHistoryMapper;
import com.youthexpedition.azit.modules.member.adapter.out.mapper.MemberTermsConsentMapper;
import com.youthexpedition.azit.modules.member.adapter.out.mapper.TermsVersionMapper;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.MemberTermsConsentHistoryRepository;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.MemberTermsConsentRepository;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.TermsVersionRepository;
import com.youthexpedition.azit.modules.member.application.port.out.LoadTermsVersionPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberTermsConsentPort;
import com.youthexpedition.azit.modules.member.domain.model.MemberTermsConsent;
import com.youthexpedition.azit.modules.member.domain.model.MemberTermsConsentHistory;
import com.youthexpedition.azit.modules.member.domain.model.TermsVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TermsPersistenceAdapter implements LoadTermsVersionPort, SaveMemberTermsConsentPort {

    private final TermsVersionRepository termsVersionRepository;
    private final MemberTermsConsentRepository memberTermsConsentRepository;
    private final MemberTermsConsentHistoryRepository memberTermsConsentHistoryRepository;
    private final TermsVersionMapper termsVersionMapper;
    private final MemberTermsConsentMapper memberTermsConsentMapper;
    private final MemberTermsConsentHistoryMapper memberTermsConsentHistoryMapper;

    @Override
    public List<TermsVersion> findAllLatest() {
        return termsVersionRepository.findAllLatest().stream()
                .map(termsVersionMapper::toDomain)
                .toList();
    }

    @Override
    public Set<Long> findConsentedVersionIdsByMemberId(Long memberId) {
        return memberTermsConsentRepository.findTermsVersionIdsByMemberId(memberId);
    }

    @Override
    public void saveAll(List<MemberTermsConsent> consents) {
        memberTermsConsentRepository.saveAll(
                consents.stream()
                        .map(memberTermsConsentMapper::toEntity)
                        .toList()
        );
    }

    @Override
    public void updateAgreedAt(Long memberId, Set<Long> versionIds, LocalDateTime agreedAt) {
        memberTermsConsentRepository.updateAgreedAt(memberId, versionIds, agreedAt);
    }

    @Override
    public void deleteByMemberIdAndVersionIds(Long memberId, Set<Long> versionIds) {
        memberTermsConsentRepository.deleteByMemberIdAndTermsVersionIdIn(memberId, versionIds);
    }

    @Override
    public void saveAllHistory(List<MemberTermsConsentHistory> histories) {
        memberTermsConsentHistoryRepository.saveAll(
                histories.stream()
                        .map(memberTermsConsentHistoryMapper::toEntity)
                        .toList()
        );
    }
}
