package com.youthexpedition.azit.infrastructure.auth.interceptor;

import com.youthexpedition.azit.infrastructure.auth.model.MemberDetails;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.application.port.out.LoadCrewMemberPort;
import com.youthexpedition.azit.modules.member.domain.model.Member;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CrewMembershipCheckInterceptorTest {

    @InjectMocks
    private CrewMembershipCheckInterceptor interceptor;

    @Mock
    private LoadCrewMemberPort loadCrewMemberPort;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        given(request.getRequestURI()).willReturn("/api/v1/products");
    }

    @Nested
    class 인증된_사용자 {

        private void setAuthentication(Long memberId) {
            Member member = Member.builder()
                    .id(memberId)
                    .status(MemberStatus.ACTIVE)
                    .build();
            MemberDetails memberDetails = new MemberDetails(member);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        void 가입된_크루가_있으면_접근_허용() throws Exception {
            // given
            setAuthentication(1L);
            given(loadCrewMemberPort.countJoinedCrewsByMemberId(1L)).willReturn(1L);

            // when
            boolean result = interceptor.preHandle(request, response, new Object());

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 가입된_크루가_없으면_예외발생() {
            // given
            setAuthentication(1L);
            given(loadCrewMemberPort.countJoinedCrewsByMemberId(1L)).willReturn(0L);

            // when & then
            assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.CREW_MEMBERSHIP_REQUIRED);
        }
    }

    @Nested
    class 인증되지_않은_사용자 {

        @Test
        void SecurityContext가_비어있으면_통과() throws Exception {
            // given - SecurityContext에 인증 정보 없음

            // when
            boolean result = interceptor.preHandle(request, response, new Object());

            // then
            assertThat(result).isTrue();
        }
    }
}
