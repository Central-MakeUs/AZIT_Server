package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoadMemberPort {
    Optional<Member> findById(Long id);
    Optional<Member> findBySocialInfo(SocialProvider socialProvider, String socialProviderId);
    Map<Long, MemberProfileDto> findAllByIds(List<Long> memberIds);
}
