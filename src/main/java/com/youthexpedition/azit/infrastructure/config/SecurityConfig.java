package com.youthexpedition.azit.infrastructure.config;

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
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // 에러 핸들러

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 인증 실패 시 처리를 위한 EntryPoint 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(securityProperties.getPermitAllPaths().toArray(String[]::new)).permitAll()
                         // 공통 허용 경로 (로그인 등)
                        .requestMatchers("/api/v1/auth/social-login/**", "/api/v1/auth/reissue").permitAll()
                        // 사용자 인증 시 상태 상관없이 허용
                        .requestMatchers(
                                "/api/v1/auth/logout", "/api/v1/members/**", "/api/v1/onboarding/**"
                        ).authenticated()
                        // 나머지 API: 정회원(ACTIVE) 상태만 접근 가능
                        .anyRequest().hasAuthority("STATUS_ACTIVE")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
