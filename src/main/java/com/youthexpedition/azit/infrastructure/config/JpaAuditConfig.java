package com.youthexpedition.azit.infrastructure.config;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 인증 정보가 없거나 인증되지 않은 경우 제외
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            // Principal이 MemberDetails 타입인지 확인
            Object principal = authentication.getPrincipal();
            if (principal instanceof MemberDetails memberDetails) {
                // MemberDetails 내의 Member 객체에서 ID를 추출하여 반환
                return Optional.ofNullable(memberDetails.getMember().getId());
            }

            // 익명 사용자(anonymousUser) 등 MemberDetails가 아닌 경우
            return Optional.empty();
        };
    }
}
