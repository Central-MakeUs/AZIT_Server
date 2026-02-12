package com.youthexpedition.azit.infrastructure.config;

import com.youthexpedition.azit.infrastructure.common.resolver.CurrentAccessTokenArgumentResolver;
import com.youthexpedition.azit.infrastructure.common.resolver.CursorPageArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SocialProviderConverter socialProviderConverter;
    private final CurrentAccessTokenArgumentResolver currentAccessTokenArgumentResolver;
    private final CursorPageArgumentResolver cursorPageArgumentResolver;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(socialProviderConverter);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentAccessTokenArgumentResolver);
        resolvers.add(cursorPageArgumentResolver);
    }
}