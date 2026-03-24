package com.youthexpedition.azit.infrastructure.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlFormatUtil {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    /**
     * 이미지 경로를 전체 URL로 변환
     * 경로가 http://로 시작하는 경우 https://로 변환하여 반환
     * 이미 https://로 시작하는 경우 그대로 반환
     * 상대 경로인 경우 CloudFront 도메인 결합
     */
    public String buildFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        // 외부 이미지인지 확인
        if (imagePath.startsWith("http")) {
            // http://로 시작하는 경우에만 https://로 치환
            if (imagePath.startsWith("http://")) {
                return imagePath.replaceFirst("http://", "https://");
            }
            return imagePath;
        }

        return cloudFrontDomain + imagePath;
    }
}
