package com.youthexpedition.azit.infrastructure.config;

import com.youthexpedition.azit.infrastructure.auth.exception.JwtAccessDeniedHandler;
import com.youthexpedition.azit.infrastructure.auth.exception.JwtAuthenticationEntryPoint;
import com.youthexpedition.azit.infrastructure.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final ApiLoggingFilter apiLoggingFilter; // api 요청 로깅

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 인증 실패 시 처리를 위한 핸들러 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401: 인증 실패
                        .accessDeniedHandler(jwtAccessDeniedHandler)           // 403: 권한 부족 (인가 실패) 처리
                )

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(securityProperties.getPermitAllPaths().toArray(String[]::new)).permitAll()
                         // 비로그인시에도 가능한 공통 허용 경로
                        .requestMatchers(
                                "/api/v1/auth/social-login/**",
                                "/api/v1/auth/reissue",
                                "/api/v1/auth/apple/notification"
                        ).permitAll()

                        // 약관 동의 전(PENDING_TERMS)·정상(ACTIVE) 회원 모두 가능
                        .requestMatchers(
                                "/api/v1/auth/logout",
                                "/api/v1/members/terms"
                        )
                        .hasAnyAuthority("STATUS_PENDING_TERMS", "STATUS_ACTIVE")

                        // 나머지 모든 API: ACTIVE 상태(약관 동의 완료, 비탈퇴) 회원만 접근 가능
                        .anyRequest().hasAuthority("STATUS_ACTIVE")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiLoggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        SecurityProperties.Cors cors = securityProperties.getCors();

        config.setAllowedOrigins(cors.getAllowedOrigins());
        config.setAllowedMethods(cors.getAllowedMethods());
        config.setAllowedHeaders(cors.getAllowedHeaders());
        config.setAllowCredentials(cors.isAllowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
