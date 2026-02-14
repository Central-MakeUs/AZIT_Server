package com.youthexpedition.azit.modules.crew.domain.model;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Crew {
    private final Long id;
    private String name;
    private CrewCategory category;
    private Region region;
    private String imageUrl;
    private String invitationCode; // 초대 코드
    private Integer memberCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static Crew create(String name, CrewCategory category, Region region, String invitationCode) {
        return Crew.builder()
                .name(name)
                .category(category)
                .region(region)
                .invitationCode(invitationCode)
                .memberCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 랜덤 코드 생성
    public static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public void addMember() {
        this.memberCount++;
    }

    public void removeMember() {
        if (this.memberCount > 1) {
            this.memberCount--;
        }
    }

}
