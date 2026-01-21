package com.youthexpedition.azit.modules.crew.domain.model;

import com.youthexpedition.azit.modules.crew.domain.model.enums.CrewCategory;
import com.youthexpedition.azit.modules.crew.domain.model.enums.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CrewTest {
    @Test
    @DisplayName("크루 생성 시 6자리의 영문 대문자 및 숫자 조합 초대 코드가 생성됨")
    void createCrew_Success() {
        // given
        String name = "아지트 러닝 크루";
        CrewCategory category = CrewCategory.RUNNING;
        Region region = Region.SEOUL;

        // when
        Crew crew = Crew.create(name, category, region);

        // then
        assertThat(crew.getName()).isEqualTo(name);
        assertThat(crew.getInvitationCode()).hasSize(6);
        assertThat(crew.getInvitationCode()).matches("^[A-Z0-9]{6}$");
    }

}