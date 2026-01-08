package com.youthexpedition.azit.modules.member.adapter.out.persistence;

import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberEntity;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.MemberRepository;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements LoadMemberPort, SaveMemberPort {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id).map(memberMapper::toDomain); // Entity -> Domain 변환
    }

    @Override
    public Optional<Member> findBySocialInfo(SocialProvider socialProvider, String socialProviderId) {
        return memberRepository.findBySocialProviderAndSocialProviderId(socialProvider, socialProviderId)
                .map(memberMapper::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberEntity entity = memberMapper.toEntity(member);
        MemberEntity savedEntity = memberRepository.save(entity);
        return memberMapper.toDomain(savedEntity);
    }

}
