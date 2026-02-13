package com.youthexpedition.azit.infrastructure.common.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlProvider {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    /**
     * 이미지 경로를 전체 URL로 변환
     * 경로가 http로 시작하는 소셜 이미지는 그대로 반환
     * 상대 경로인 경우 CloudFront 도메인 결합
     */
    public String buildFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        // 외부 이미지인지 확인
        if (imagePath.startsWith("http")) {
            return imagePath;
        }

        return cloudFrontDomain + imagePath;
    }
}
