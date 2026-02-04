package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;
import com.youthexpedition.azit.modules.member.domain.model.enums.DeliveryAddressErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryDeliveryAddressServiceTest {
    @InjectMocks
    private DeliveryDeliveryAddressService deliveryAddressService;

    @Mock
    private LoadDeliveryAddressPort loadDeliveryAddressPort;

    @Mock
    private SaveDeliveryAddressPort saveDeliveryAddressPort;

    @Nested
    @DisplayName("주소 등록")
    class registerDeliveryAddress {
        @Test
        @DisplayName("성공: 사용자의 첫 배송지 등록 시, 요청과 무관하게 기본 배송지로 설정된다.")
        void registerAddress_first_time_is_always_default() {
            // given
            RegisterAddressCommand command = RegisterAddressCommand.of(
                    1L, "김철수", "010-1234-5678", "06234",
                    "서울시 강남구", "101호", false // 요청은 false로 보냄
            );

            given(loadDeliveryAddressPort.existsByMemberId(1L)).willReturn(false);

            // when
            deliveryAddressService.registerDeliveryAddress(command);

            // then
            verify(saveDeliveryAddressPort).save(argThat(DeliveryAddress::isDefault));
        }

        @Test
        @DisplayName("성공: 새로운 주소를 기본 배송지로 등록하면, 기존 기본 배송지는 일반 주소로 변경된다.")
        void registerAddress_change_default() {
            // given
            Long memberId = 1L;
            RegisterAddressCommand command = RegisterAddressCommand.of(
                    memberId, "이영희", "010-5678-1234", "06234",
                    "서울시 서초구", "202호", true // 새 주소를 기본으로 설정
            );

            DeliveryAddress oldDefault = DeliveryAddress.builder()
                    .memberId(memberId).isDefault(true).build();

            given(loadDeliveryAddressPort.existsByMemberId(memberId)).willReturn(true);
            given(loadDeliveryAddressPort.findDefaultByMemberId(memberId)).willReturn(Optional.of(oldDefault));

            // when
            deliveryAddressService.registerDeliveryAddress(command);

            // then
            assertThat(oldDefault.isDefault()).isFalse(); // 기존 주소는 해제됨
            verify(saveDeliveryAddressPort, times(2)).save(any(DeliveryAddress.class)); // 기존 수정 + 신규 저장
        }
    }

    @Nested
    @DisplayName("주소 수정")
    class updateDeliveryAddress {
        @Test
        @DisplayName("성공: 본인의 주소를 정상적인 데이터로 수정하면 성공한다.")
        void updateAddress_success() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;
            UpdateAddressCommand command = new UpdateAddressCommand(
                    memberId, addressId, "홍길동", "010-9999-8888", "12345",
                    "경기도 성남시", "판교역로 1", false
            );

            DeliveryAddress existingDeliveryAddress = DeliveryAddress.builder()
                    .id(addressId)
                    .memberId(memberId)
                    .isDefault(false)
                    .build();

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.of(existingDeliveryAddress));

            // when
            deliveryAddressService.updateDeliveryAddress(command);

            // then
            assertThat(existingDeliveryAddress.getRecipientName()).isEqualTo("홍길동");
            assertThat(existingDeliveryAddress.getBaseAddress()).isEqualTo("경기도 성남시");
            verify(saveDeliveryAddressPort).save(existingDeliveryAddress);
        }

        @Test
        @DisplayName("실패: 타인의 주소를 수정하려고 하면 FORBIDDEN_ADDRESS_ACCESS 예외가 발생한다.")
        void updateAddress_fail_forbidden() {
            // given
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Long addressId = 100L;
            UpdateAddressCommand command = new UpdateAddressCommand(
                    memberId, addressId, "수정", "010-1111-2222", "12345", "주소", "상세", false
            );

            DeliveryAddress othersDeliveryAddress = DeliveryAddress.builder()
                    .id(addressId)
                    .memberId(otherMemberId) // 소유자가 다름
                    .build();

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.of(othersDeliveryAddress));

            // when & then
            assertThatThrownBy(() -> deliveryAddressService.updateDeliveryAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", DeliveryAddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주소 ID로 수정 시 ADDRESS_NOT_FOUND 예외가 발생한다.")
        void updateAddress_fail_not_found() {
            // given
            Long addressId = 999L;
            UpdateAddressCommand command = new UpdateAddressCommand(
                    1L, addressId, "수정", "010-1111-2222", "12345", "주소", "상세", false
            );

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deliveryAddressService.updateDeliveryAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", DeliveryAddressErrorCode.ADDRESS_NOT_FOUND);
        }

        @Test
        @DisplayName("성공: 일반 주소를 '기본 배송지'로 수정하면 기존 기본 배송지는 해제된다.")
        void updateAddress_change_to_default() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;
            UpdateAddressCommand command = new UpdateAddressCommand(
                    memberId, addressId, "수정", "010-1111-2222", "12345", "주소", "상세", true // 기본으로 변경 요청
            );

            DeliveryAddress targetDeliveryAddress = DeliveryAddress.builder().id(addressId).memberId(memberId).isDefault(false).build();
            DeliveryAddress oldDefaultDeliveryAddress = DeliveryAddress.builder().id(200L).memberId(memberId).isDefault(true).build();

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.of(targetDeliveryAddress));
            given(loadDeliveryAddressPort.findDefaultByMemberId(memberId)).willReturn(Optional.of(oldDefaultDeliveryAddress));

            // when
            deliveryAddressService.updateDeliveryAddress(command);

            // then
            assertThat(targetDeliveryAddress.isDefault()).isTrue();
            assertThat(oldDefaultDeliveryAddress.isDefault()).isFalse(); // 기존 배송지는 해제
            verify(saveDeliveryAddressPort, times(2)).save(any(DeliveryAddress.class));
        }
    }

    @Nested
    @DisplayName("주소 삭제")
    class deleteDeliveryAddress {
        @Test
        @DisplayName("성공: 본인의 배송지를 삭제하면 정상적으로 삭제 처리된다.")
        void deleteAddress_success() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;

            DeliveryAddress existingDeliveryAddress = DeliveryAddress.builder()
                    .id(addressId)
                    .memberId(memberId)
                    .isDefault(false)
                    .build();

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.of(existingDeliveryAddress));

            // when
            deliveryAddressService.deleteDeliveryAddress(memberId, addressId);

            // then
            verify(saveDeliveryAddressPort).delete(existingDeliveryAddress);
        }

        @Test
        @DisplayName("성공: MVP 정책에 따라 기본 배송지도 삭제가 가능하다.")
        void deleteAddress_default_success() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;

            DeliveryAddress defaultDeliveryAddress = DeliveryAddress.builder()
                    .id(addressId)
                    .memberId(memberId)
                    .isDefault(true) // 기본 배송지
                    .build();

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.of(defaultDeliveryAddress));

            // when
            deliveryAddressService.deleteDeliveryAddress(memberId, addressId);

            // then
            verify(saveDeliveryAddressPort).delete(defaultDeliveryAddress);
        }

        @Test
        @DisplayName("실패: 타인의 배송지를 삭제하려고 시도할 경우 FORBIDDEN_ADDRESS_ACCESS 예외가 발생한다.")
        void deleteAddress_fail_forbidden() {
            // given
            Long myId = 1L;
            Long otherMemberId = 2L;
            Long addressId = 100L;

            DeliveryAddress othersDeliveryAddress = DeliveryAddress.builder()
                    .id(addressId)
                    .memberId(otherMemberId) // 소유자가 다름
                    .build();

            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.of(othersDeliveryAddress));

            // when & then
            assertThatThrownBy(() -> deliveryAddressService.deleteDeliveryAddress(myId, addressId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", DeliveryAddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주소를 삭제하려고 시도할 경우 ADDRESS_NOT_FOUND 예외가 발생한다.")
        void deleteAddress_fail_not_found() {
            // given
            Long addressId = 999L;
            given(loadDeliveryAddressPort.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deliveryAddressService.deleteDeliveryAddress(1L, addressId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", DeliveryAddressErrorCode.ADDRESS_NOT_FOUND);
        }
    }


}