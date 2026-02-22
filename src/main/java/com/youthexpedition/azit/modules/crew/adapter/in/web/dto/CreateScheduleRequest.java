package com.youthexpedition.azit.modules.crew.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.crew.application.port.in.command.CreateScheduleCommand;
import com.youthexpedition.azit.modules.crew.domain.model.enums.RunType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record CreateScheduleRequest(
        @Schema(description = "런 타이틀")
        @NotBlank(message = "타이틀은 필수입니다.")
        @Size(max = 15, message = "타이틀은 최대 15자까지 작성 가능합니다.")
        String title,

        @Schema(description = "런 종류 (REGULAR: 정기런, LIGHTNING: 번개런)")
        @NotNull(message = "런 종류는 필수입니다.")
        RunType runType,

        @Schema(description = "일정 일시 (yyyy-MM-dd HH:mm:ss)")
        @NotNull(message = "일정 일시는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime meetingAt,

        @Schema(description = "집합 장소 명칭")
        @NotBlank(message = "장소 명칭은 필수입니다.")
        String locationName,

        @Schema(description = "집합 장소 주소")
        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @Schema(description = "세부 장소 (유저 직접 입력)")
        @NotBlank(message = "세부 장소는 필수입니다.")
        @Size(max = 15, message = "세부 장소는 최대 15자까지 작성 가능합니다.")
        String detailedLocation,

        @Schema(description = "위도")
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @Schema(description = "경도")
        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        @Schema(description = "목표 거리 (km)")
        @Positive(message = "거리는 양수여야 합니다.")
        Double distance,

        @Schema(description = "목표 페이스 (분/km)")
        @Positive(message = "페이스는 양수여야 합니다.")
        Double pace,

        @Schema(description = "최대 모집 인원")
        @Min(value = 1, message = "최소 1명 이상이어야 합니다.")
        @Max(value = 999, message = "최대 999명까지 가능합니다.")
        Integer maxParticipants,

        @Schema(description = "상세 설명")
        String description,

        @Schema(description = "준비물 리스트 (각 최대 15자, 최대 5개)", example = "[\"러닝화\", \"생수\"]")
        @Size(max = 5, message = "준비물은 최대 5개까지 등록 가능합니다.")
        List<@Size(max = 15, message = "준비물 내용은 최대 15자까지 가능합니다.") String> supplies
) {
    public CreateScheduleCommand toCommand(Long crewId, Long memberId) {
        return CreateScheduleCommand.of(
                crewId,
                memberId,
                title,
                runType,
                meetingAt,
                locationName,
                address,
                detailedLocation,
                latitude,
                longitude,
                distance,
                pace,
                maxParticipants,
                description,
                supplies
        );
    }
}
