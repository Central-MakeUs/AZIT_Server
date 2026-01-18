package com.youthexpedition.azit.modules.auth.application.service;

import com.youthexpedition.azit.modules.auth.application.port.out.TokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenManagementService 단위 테스트")
class TokenManagementServiceTest {
    @Mock
    private TokenPort tokenPort;

    @InjectMocks
    private TokenManagementService tokenManagementService;


    @Nested
    @DisplayName("로그아웃")
    class Logout {
        @Test
        @DisplayName("성공")
        void logout_success() {
            // given
            Long memberId = 1L;
            String accessToken = "accessToken";

            // when
            tokenManagementService.logout(memberId, accessToken);

            // then
            verify(tokenPort, times(1)).deleteByMemberId(memberId);
            verify(tokenPort, times(1)).addToBlacklist(accessToken, "logout");
        }
    }
}
