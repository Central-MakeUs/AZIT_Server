package com.youthexpedition.azit.modules.crew.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.domain.model.CrewSchedule;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewMemberRole;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import com.youthexpedition.azit.modules.crew.domain.model.enums.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record CrewScheduleDetailResponse(
        @Schema(description = "일정 ID")
        Long scheduleId,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "러닝 타입")
        RunType runType,
        @Schema(description = "모임 시간")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingAt,
        @Schema(description = "장소 정보")
        LocationInfoResponse locationInfo,
        @Schema(description = "일정 설명")
        String description,
        @Schema(description = "목표 거리 (km)")
        Double distance,
        @Schema(description = "목표 페이스")
        Double pace,
        @Schema(description = "최대 인원")
        Integer maxParticipants,
        @Schema(description = "현재 참여 인원")
        Integer currentParticipants,
        @Schema(description = "준비물 리스트")
        List<String> supplies,
        @Schema(description = "일정 상태")
        ScheduleStatus status,
        @Schema(description = "내가 생성한 일정인지 여부")
        boolean isMine,
        @Schema(description = "내가 참여 중인 일정인지 여부")
        boolean isParticipating,
        @Schema(description = "참여 멤버 미리보기 리스트(최대 6명)")
        List<ParticipantResponse> participants,
        @Schema(description = "참여자 명단이 더 있는지 여부 (6명 초과 시 true)")
        boolean hasMoreParticipants
) {
        public record ParticipantResponse(
                @Schema(description = "멤버 ID")
                Long memberId,
                @Schema(description = "닉네임")
                String nickname,
                @Schema(description = "프로필 이미지 URL")
                String profileImageUrl,
                @Schema(description = "크루 내 역할")
                CrewMemberRole role,
                @Schema(description = "일정 생성자 여부")
                boolean isCreator
        ) {
                public static ParticipantResponse of(
                        Long memberId,
                        String nickname,
                        String profileImageUrl,
                        CrewMemberRole role,
                        boolean isCreator
                ) {
                        return new ParticipantResponse(memberId, nickname, profileImageUrl, role, isCreator);
                }
        }

        public static CrewScheduleDetailResponse of(
                CrewSchedule schedule,
                Long currentMemberId,
                List<ParticipantResponse> participants,
                boolean hasMoreParticipants
        ) {
                return new CrewScheduleDetailResponse(
                        schedule.getId(),
                        schedule.getTitle(),
                        schedule.getRunType(),
                        schedule.getMeetingAt(),
                        LocationInfoResponse.of(schedule.getLocation()),
                        schedule.getDescription(),
                        schedule.getDistance(),
                        schedule.getPace(),
                        schedule.getMaxParticipants(),
                        participants.size(),
                        schedule.getSupplies(),
                        schedule.getStatus(),
                        schedule.getCreatorId().equals(currentMemberId), // 생성자 여부 판단
                        schedule.isParticipating(currentMemberId), // 참여 여부 판단
                        participants,
                        hasMoreParticipants
                );
        }
}