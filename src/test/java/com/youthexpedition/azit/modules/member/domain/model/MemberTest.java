package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.modules.member.domain.model.enums.SocialProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
}
