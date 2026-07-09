package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberErrorCode;
import com.youthexpedition.azit.modules.member.domain.model.enums.MemberStatus;
import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Member 도메인 단위 테스트")
class MemberTest {

    @Test
    @DisplayName("성공: 닉네임이 정상적으로 변경된다.")
    void updateNickname_success() {
        // given
        Member member = Member.create(SocialProvider.KAKAO, "socialId", "oldNickname", "test@example.com", true, "imageUrl");

        // when
        member.updateNickname("newNickname");

        // then
        assertThat(member.getNickname()).isEqualTo("newNickname");
    }

    @Test
    @DisplayName("성공: 닉네임을 동일한 값으로 변경해도 정상 처리된다.")
    void updateNickname_sameValue() {
        // given
        Member member = Member.create(SocialProvider.KAKAO, "socialId", "sameNickname", "test@example.com", true, "imageUrl");

        // when
        member.updateNickname("sameNickname");

        // then
        assertThat(member.getNickname()).isEqualTo("sameNickname");
    }

    @Test
    @DisplayName("성공: 탈퇴 시 상태가 WITHDRAWN으로 변경되고 탈퇴 시점이 기록되며 애플 리프레시 토큰은 보존된다.")
    void withdraw_success_recordsWithdrawnAtAndKeepsAppleRefreshToken() {
        // given
        Member member = Member.create(SocialProvider.APPLE, "appleSub", "nickname", "test@example.com", true, "imageUrl");
        member.updateAppleRefreshToken("appleRefreshToken");
        LocalDateTime withdrawnAt = LocalDateTime.of(2026, 7, 9, 12, 0);

        // when
        member.withdraw(withdrawnAt);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(member.getWithdrawnAt()).isEqualTo(withdrawnAt);
        assertThat(member.getAppleRefreshToken()).isEqualTo("appleRefreshToken"); // 파기 배치의 연동 해제용으로 보존
    }

    @Test
    @DisplayName("성공: 유예기간 내 재활성화 시 ACTIVE 상태가 되고 탈퇴 시점이 초기화된다.")
    void reactivate_success_clearsWithdrawnAt() {
        // given
        Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");
        LocalDateTime withdrawnAt = LocalDateTime.of(2026, 7, 9, 12, 0);
        member.withdraw(withdrawnAt);

        // when - 탈퇴 후 10일 뒤 재로그인
        member.reactivate(withdrawnAt.plusDays(10));

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getWithdrawnAt()).isNull();
    }

    @Test
    @DisplayName("실패: 유예기간(30일)이 만료된 회원은 재활성화할 수 없다.")
    void reactivate_throwsException_whenGracePeriodExpired() {
        // given
        Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");
        LocalDateTime withdrawnAt = LocalDateTime.of(2026, 7, 9, 12, 0);
        member.withdraw(withdrawnAt);

        // when & then - 탈퇴 후 31일 뒤 재로그인
        assertThatThrownBy(() -> member.reactivate(withdrawnAt.plusDays(31)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("성공: 유예기간 마지막 날(30일째)까지는 재활성화할 수 있다.")
    void reactivate_success_onLastDayOfGracePeriod() {
        // given
        Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");
        LocalDateTime withdrawnAt = LocalDateTime.of(2026, 7, 9, 12, 0);
        member.withdraw(withdrawnAt);

        // when - 정확히 30일 뒤 재로그인
        member.reactivate(withdrawnAt.plusDays(30));

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("성공: 탈퇴 상태가 아닌 회원은 재활성화 호출 시 상태가 변경되지 않는다.")
    void reactivate_noChange_whenNotWithdrawn() {
        // given
        Member member = Member.create(SocialProvider.KAKAO, "socialId", "nickname", "test@example.com", true, "imageUrl");

        // when
        member.reactivate(LocalDateTime.of(2026, 7, 9, 12, 0));

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING_TERMS);
    }
}
