package com.youthexpedition.azit.infrastructure.config;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SocialProviderConverter implements Converter<String, SocialProvider> {
    @Override
    public SocialProvider convert(String source) {
        // 소문자로 들어온 "kakao" 등을 대문자로 변환하여 Enum 매핑
        return SocialProvider.valueOf(source.toUpperCase());
    }
}