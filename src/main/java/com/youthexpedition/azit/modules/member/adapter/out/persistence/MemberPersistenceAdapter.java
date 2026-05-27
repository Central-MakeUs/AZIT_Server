package com.youthexpedition.azit.modules.member.adapter.out.persistence;

import com.youthexpedition.azit.modules.member.adapter.out.mapper.MemberMapper;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.entity.MemberEntity;
import com.youthexpedition.azit.modules.member.adapter.out.persistence.repository.MemberRepository;
import com.youthexpedition.azit.modules.member.application.port.out.LoadMemberPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveMemberPort;
import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements LoadMemberPort, SaveMemberPort {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id)
                .map(memberMapper::toDomain); // Entity -> Domain 변환
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

    @Override
    public void deleteById(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    @Override
    public Map<Long, MemberProfileDto> findAllByIds(List<Long> memberIds) {
        return memberRepository.findAllById(memberIds).stream()
                .filter(entity -> entity.getStatus() != MemberStatus.WITHDRAWN)
                .collect(Collectors.toMap(
                        MemberEntity::getId,
                        entity -> MemberProfileDto.of(entity.getId(), entity.getNickname(), entity.getProfileImageUrl())
                ));
    }

}
