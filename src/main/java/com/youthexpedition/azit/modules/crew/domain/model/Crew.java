package com.youthexpedition.azit.modules.crew.domain.model;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewStatus;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class Crew {
    private final Long id;
    private String name;
    private CrewCategory category;
    private Region region;
    private String imageUrl;
    private String description;
    private String invitationCode; // 초대 코드
    private Integer memberCount;
    private CrewStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 서비스·관리자·제휴 사칭 방지 예약어 (대소문자 구분 없이 포함 여부 검사)
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            // 서비스 및 관리자 사칭 방지
            "azit", "아지트", "관리자", "어드민", "admin", "공식", "official", "운영진", "스태프", "staff",
            // 제휴·스폰서 사칭 방지
            "스폰서", "sponsor", "제휴", "파트너", "partner"
    );

    public static Crew create(String name, CrewCategory category, Region region, String imageUrl, String invitationCode) {
        String trimmedName = name.trim(); // 공백 제거
        validateName(trimmedName);
        return Crew.builder()
                .name(trimmedName)
                .category(category)
                .region(region)
                .imageUrl(imageUrl)
                .invitationCode(invitationCode)
                .memberCount(1)
                .status(CrewStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static void validateName(String name) {
        // 크루 이름은 한글, 숫자, 영어만 가능 (특수문자 제한)
        if (!name.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new BusinessException(CrewErrorCode.INVALID_CREW_NAME_CHARACTERS);
        }

        String lowerName = name.toLowerCase();
        // 불가 예약어 검사
        boolean hasReservedKeyword = RESERVED_KEYWORDS.stream().anyMatch(lowerName::contains);
        if (hasReservedKeyword) {
            throw new BusinessException(CrewErrorCode.RESERVED_CREW_NAME_KEYWORD);
        }
    }

    // 랜덤 코드 생성
    public static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public void increaseMemberCount() {
        this.memberCount++;
    }

    public void decreaseMemberCount() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }

        if (this.memberCount == 0) {
            this.status = CrewStatus.INACTIVE;
        }
    }

    // 크루 해산
    public void dissolve(LocalDateTime now) {
        this.memberCount = 0;
        this.status = CrewStatus.DISSOLVED;
        this.updatedAt = now;
    }

    public boolean isDissolved() {
        return this.status == CrewStatus.DISSOLVED;
    }

    // 초대 코드 재발급
    public void updateInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateInfo(String name, String description) {
        String trimmedName = name.trim();
        validateName(trimmedName);
        this.name = trimmedName;
        this.description = description;
    }
}
