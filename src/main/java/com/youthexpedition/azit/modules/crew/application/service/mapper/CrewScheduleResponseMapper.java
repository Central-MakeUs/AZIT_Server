package com.youthexpedition.azit.modules.crew.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.crew.application.port.in.dto.CrewScheduleDetailResponse;
import com.youthexpedition.azit.modules.crew.application.port.out.query.MemberProfileDto;
import com.youthexpedition.azit.modules.crew.domain.model.CrewMember;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CrewScheduleResponseMapper {
    private final ImageUrlFormatUtil imageUrlFormatUtil;

    public CrewScheduleDetailResponse toDetailResponse(
            CrewSchedule schedule, Long currentMemberId, Map<Long, MemberProfileDto> profileMap, Map<Long, CrewMember> crewMemberMap) {
        // 전체 참여자 정보를 조합하여 정렬
        List<CrewScheduleDetailResponse.ParticipantResponse> allParticipants = schedule.getParticipants().stream()
                // 데이터가 존재하는 참여자들만 필터링
                .filter(participant -> profileMap.containsKey(participant.getMemberId()) && crewMemberMap.containsKey(participant.getMemberId()))
                .map(participant -> {
                    Long id = participant.getMemberId();
                    MemberProfileDto profile = profileMap.get(id);
                    CrewMember crewMember = crewMemberMap.get(id);
                    return CrewScheduleDetailResponse.ParticipantResponse.of(
                            id,
                            profile.nickname(),
                            imageUrlFormatUtil.buildFullImageUrl(profile.profileImageUrl()),
                            crewMember.getRole(),
                            id.equals(schedule.getCreatorId()),
                            participant.getCreatedAt()
                    );
                })
                // 일정 생성자 -> 리더 -> 멤버 신청 시간 순
                .sorted(Comparator.comparing((CrewScheduleDetailResponse.ParticipantResponse p) -> p.isCreator() ? 0 : 1)
                        .thenComparing(p -> p.role() == CrewMemberRole.LEADER ? 0 : 1)
                        .thenComparing(CrewScheduleDetailResponse.ParticipantResponse::participatedAt))
                .toList();

        // 미리보기용으로 6명 추출
        List<CrewScheduleDetailResponse.ParticipantResponse> previewParticipants = allParticipants.stream()
                .limit(6)
                .toList();

        // 더보기 플래그 결정 (전체 인원이 6명보다 많으면 true)
        boolean hasMore = allParticipants.size() > 6;

        return CrewScheduleDetailResponse.of(schedule, currentMemberId, previewParticipants, hasMore);
    }

}
