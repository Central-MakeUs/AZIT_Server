package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.domain.model.enums.DeliveryAddressErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DeliveryAddressTest {
    @Test
    @DisplayName("성공: 필수 항목이 모두 입력되면 배송지 객체가 생성된다.")
    void create_success() {
        // given & when
        DeliveryAddress deliveryAddress = DeliveryAddress.create(
                1L, "김철수", "010-1234-5678", "06234",
                "서울시 강남구", "101호", true
        );

        // then
        assertThat(deliveryAddress.getRecipientName()).isEqualTo("김철수");
        assertThat(deliveryAddress.getBaseAddress()).isEqualTo("서울시 강남구");
    }

    @Test
    @DisplayName("실패: 상세 주소가 누락되면 BusinessException(INVALID_ADDRESS_INPUT)이 발생한다.")
    void create_fail_detailAddress_empty() {
        // given
        String emptyDetailAddress = "";

        // when & then
        assertThatThrownBy(() -> DeliveryAddress.create(
                1L, "김철수", "010-1234-5678", "06234",
                "서울시 강남구", emptyDetailAddress, true
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DeliveryAddressErrorCode.INVALID_ADDRESS_INPUT);
    }
}