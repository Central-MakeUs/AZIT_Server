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
            return imagePath.replaceFirst("http://", "https://");
        }

        return cloudFrontDomain + imagePath;
    }

    /**
     * 이미지 경로에서 S3 Key 추출
     * CloudFront URL: https://azitcrew.com/profile/123/2026-04-07_550e8400.jpg → profile/123/2026-04-07_550e8400.jpg
     * 상대 경로:      /profile/123/2026-04-07_550e8400.jpg                     → profile/123/2026-04-07_550e8400.jpg
     */
    public String extractS3Key(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        // trailing slash 정규화 (설정값 끝에 '/' 포함 여부와 무관하게 동작)
        String normalizedDomain = cloudFrontDomain.endsWith("/")
                ? cloudFrontDomain.substring(0, cloudFrontDomain.length() - 1)
                : cloudFrontDomain;
        if (imageUrl.startsWith(normalizedDomain + "/")) {
            return imageUrl.substring(normalizedDomain.length() + 1);
        }
        if (imageUrl.startsWith("/")) {
            return imageUrl.substring(1);
        }
        return null;
    }
}
