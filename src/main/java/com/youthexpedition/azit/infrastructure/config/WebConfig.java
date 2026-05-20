package com.youthexpedition.azit.infrastructure.config;

import com.youthexpedition.azit.infrastructure.auth.interceptor.CrewMembershipCheckInterceptor;
import com.youthexpedition.azit.infrastructure.common.resolver.CurrentAccessTokenArgumentResolver;
import com.youthexpedition.azit.infrastructure.common.resolver.CursorPageArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SocialProviderConverter socialProviderConverter;
    private final CurrentAccessTokenArgumentResolver currentAccessTokenArgumentResolver;
    private final CursorPageArgumentResolver cursorPageArgumentResolver;
    private final CrewMembershipCheckInterceptor crewMembershipCheckInterceptor;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(socialProviderConverter);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentAccessTokenArgumentResolver);
        resolvers.add(cursorPageArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(crewMembershipCheckInterceptor)
                .addPathPatterns(
                        // 일정 관련 API (크루 일정)
                        "/api/v1/crews/*/schedules/**",
                        // 일정 관련 API (내 일정/출석)
                        "/api/v1/members/me/schedules",
                        "/api/v1/members/me/schedules/**",
                        "/api/v1/members/me/check-in-status",
                        "/api/v1/members/me/attendances",
                        "/api/v1/members/me/attendances/**",
                        // 스토어 관련 API
                        "/api/v1/products/**",
                        "/api/v1/orders/**",
                        "/api/v1/carts/**"
                );
    }
}