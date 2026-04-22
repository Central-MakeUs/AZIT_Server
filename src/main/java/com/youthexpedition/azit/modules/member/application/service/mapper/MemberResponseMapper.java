package com.youthexpedition.azit.modules.member.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.image.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.domain.model.Crew;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberStatus;
import com.youthexpedition.azit.modules.member.application.port.in.dto.MyInfoResponse;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberResponseMapper {

    private final ImageUrlFormatUtil imageUrlFormatUtil;

    public MyInfoResponse toMyPageResponse(Member member, CrewMember crewMember, Crew crew) {
        // 아직 소속 크루가 없는 경우
        if (crewMember == null || crew == null) {
            return MyInfoResponse.of(
                    member.getId(),
                    member.getNickname(),
                    member.getStatus(),
                    null, null, null, null, null,
                    imageUrlFormatUtil.buildFullImageUrl(member.getProfileImageUrl()),
                    member.getTotalAttendanceCount(),
                    member.getTotalPoints()
            );
        }

        // 리더인 경우에만 초대 코드 노출
        // 방출된 크루(EXITED)에서는 초대 코드가 보이지 않도록 처리
        String invitationCode = (crewMember.getRole() == CrewMemberRole.LEADER && crewMember.getStatus() == CrewMemberStatus.JOINED)
                ? crew.getInvitationCode() : null;

        return MyInfoResponse.of(
                member.getId(),
                member.getNickname(),
                member.getStatus(),
                crewMember.getCrewId(),
                crew.getName(),
                invitationCode,
                imageUrlFormatUtil.buildFullImageUrl(crew.getImageUrl()),
                crewMember.getRole(),
                imageUrlFormatUtil.buildFullImageUrl(member.getProfileImageUrl()),
                member.getTotalAttendanceCount(),
                member.getTotalPoints()
        );
    }
}