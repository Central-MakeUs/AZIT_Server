package com.youthexpedition.azit.modules.crew.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewErrorCode;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrewTest {

    @Test
    @DisplayName("크루 생성 시 6자리의 영문 대문자 및 숫자 조합 초대 코드가 생성된다.")
    void create_success() {
        // given
        String name = "서울 러닝 크루";
        CrewCategory category = CrewCategory.RUNNING;
        Region region = Region.SEOUL;
        String invitationCode = "ABC123";
        String imageUrl = "testUrl.png";

        // when
        Crew crew = Crew.create(name, category, region, imageUrl, invitationCode);

        // then
        assertThat(crew.getName()).isEqualTo(name);
        assertThat(crew.getInvitationCode()).hasSize(6);
        assertThat(crew.getInvitationCode()).matches("^[A-Z0-9]{6}$");
    }

    @Nested
    @DisplayName("크루명 예약어 필터링")
    class ReservedKeywordTest {

        @ParameterizedTest(name = "예약어 포함 크루명 \"{0}\" 으로 생성 시 예외 발생")
        @ValueSource(strings = {
                "AZIT 러닝", "azit크루", "아지트크루", "관리자크루", "어드민크루",
                "Admin크루", "admin크루", "공식크루", "Official러닝", "official크루",
                "운영진크루", "스태프크루", "Staff러닝", "staff크루",
                "스폰서크루", "Sponsor러닝", "sponsor크루", "제휴크루", "파트너크루",
                "Partner러닝", "partner크루"
        })
        void create_throwsException_whenNameContainsReservedKeyword(String name) {
            // when & then
            assertThatThrownBy(() ->
                    Crew.create(name, CrewCategory.RUNNING, Region.SEOUL, "img.png", "ABC123"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CrewErrorCode.RESERVED_CREW_NAME_KEYWORD);
        }

        @Test
        @DisplayName("크루명 수정 시 예약어가 포함되면 예외가 발생한다.")
        void updateInfo_throwsException_whenNameContainsReservedKeyword() {
            // given
            Crew crew = Crew.create("서울 러닝 크루", CrewCategory.RUNNING, Region.SEOUL, "img.png", "ABC123");

            // when & then
            assertThatThrownBy(() -> crew.updateInfo("AZIT 공식 크루", "설명"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CrewErrorCode.RESERVED_CREW_NAME_KEYWORD);
        }

        @Test
        @DisplayName("예약어가 없는 크루명으로 수정 시 정상 반영된다.")
        void updateInfo_success_whenNameHasNoReservedKeyword() {
            // given
            Crew crew = Crew.create("서울 러닝 크루", CrewCategory.RUNNING, Region.SEOUL, "img.png", "ABC123");

            // when
            crew.updateInfo("한강 러닝 크루", "한강에서 달리는 크루");

            // then
            assertThat(crew.getName()).isEqualTo("한강 러닝 크루");
        }
    }
}