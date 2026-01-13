package com.youthexpedition.azit.modules.crew.domain.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Crew {
    private final Long id;
    private String name;
    private String description;
    private String region;
    private String profileImageUrl;
    private String joiningQuestion;
    private String bankName;
    private String accountNumber;
    private String invitationCode; // 초대 코드
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 초대 코드 검증
    public boolean verifyInvitationCode(String code) {
        return this.invitationCode.equals(code);
    }
}
