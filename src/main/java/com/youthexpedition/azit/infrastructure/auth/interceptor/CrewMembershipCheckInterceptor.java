package com.youthexpedition.azit.infrastructure.auth.interceptor;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 크루 가입 여부를 확인하는 인터셉터.
 * JOINED 상태인 크루가 하나도 없는 회원은 일정/스토어 관련 API에 접근할 수 없습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrewMembershipCheckInterceptor implements HandlerInterceptor {

    private final LoadCrewMemberPort loadCrewMemberPort;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 없거나 principal이 MemberDetails가 아닌 경우 통과 (Spring Security가 처리)
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberDetails memberDetails)) {
            return true;
        }

        Long memberId = memberDetails.getMember().getId();
        long joinedCrewCount = loadCrewMemberPort.countJoinedCrewsByMemberId(memberId);

        if (joinedCrewCount == 0) {
            log.warn("[CREW_MEMBERSHIP_CHECK] memberId: {} 가입된 크루가 없어 접근이 제한됩니다. URI: {}",
                    memberId, request.getRequestURI());
            throw new BusinessException(MemberErrorCode.CREW_MEMBERSHIP_REQUIRED);
        }

        return true;
    }
}
