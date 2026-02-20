package com.youthexpedition.azit.modules.crew.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CrewScheduleMember {
    private final Long id;
    private final Long memberId;
    private final LocalDateTime createdAt;
}
