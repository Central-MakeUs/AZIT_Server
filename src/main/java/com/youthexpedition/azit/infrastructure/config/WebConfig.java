package com.youthexpedition.azit.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SocialProviderConverter socialProviderConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(socialProviderConverter);
    }
}