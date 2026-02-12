package com.youthexpedition.azit.infrastructure.provider;

import com.youthexpedition.azit.modules.member.domain.model.provider.ProfileImageProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConfigurationProperties(prefix = "default.member")
@Getter
@Setter
public class S3ProfileImageProvider implements ProfileImageProvider {

    private List<String> profileImages;

    @Override
    public String getRandomDefaultImage() {
        if (profileImages == null || profileImages.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(profileImages.size());
        return profileImages.get(randomIndex);
    }
}
