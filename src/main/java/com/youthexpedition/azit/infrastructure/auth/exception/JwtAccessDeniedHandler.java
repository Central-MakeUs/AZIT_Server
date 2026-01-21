package com.youthexpedition.azit.infrastructure.auth.exception;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import com.youthexpedition.azit.infrastructure.common.response.code.BaseErrorCode;
import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 403 Forbidden 인가 실패 핸들러
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseSender errorResponseSender;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "ANONYMOUS";
        BaseErrorCode errorCode = determineErrorCode();

        log.warn("인가 실패, UserId: {}, Message: {}", username, accessDeniedException.getMessage());
        errorResponseSender.send(request, response, errorCode);
    }

    private BaseErrorCode determineErrorCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 MemberDetails가 아닌 경우 (일반적인 권한 부족)
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberDetails principal)) {
            return CommonErrorCode.FORBIDDEN_ERROR;
        }

        MemberStatus status = principal.getMember().getStatus();

        // 사용자의 상태가 ACTIVE가 아니라면 회원 상태 문제로 간주
        if (status != MemberStatus.ACTIVE) {
            return CommonErrorCode.INVALID_MEMBER_STATUS;
        }

        // ACTIVE 상태임에도 403이 발생했다면, 이는 순수하게 권한이 부족한 경우 (예: ADMIN 페이지 접근)
        return CommonErrorCode.FORBIDDEN_ERROR;
    }
}
