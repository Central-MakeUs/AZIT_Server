package com.youthexpedition.azit.infrastructure.common.util;

import com.youthexpedition.azit.modules.crew.domain.model.provider.CrewImageProvider;
import com.youthexpedition.azit.modules.member.domain.model.provider.ProfileImageProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConfigurationProperties(prefix = "default")
@Getter
@Setter
public class S3ProfileImageUtil implements ProfileImageProvider, CrewImageProvider {

    private MemberConfig member;
    private CrewConfig crew;

    @Getter @Setter
    public static class MemberConfig {
        private List<String> profileImages;
    }

    @Getter @Setter
    public static class CrewConfig {
        private String image;
    }

    // 멤버 프로필용: 랜덤 이미지 반환
    @Override
    public String getRandomDefaultImage() {
        List<String> images = member.getProfileImages();
        if (images == null || images.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(images.size());
        return images.get(randomIndex);
    }

    // 크루용: 단일 기본 이미지 반환
    @Override
    public String getCrewDefaultImage() {
        if (crew == null) {
            return null;
        }
        return crew.getImage();
    }
}
