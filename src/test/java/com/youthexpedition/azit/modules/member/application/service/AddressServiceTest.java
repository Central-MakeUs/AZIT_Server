package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import com.youthexpedition.azit.modules.member.domain.model.enums.AddressErrorCode;
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
class AddressServiceTest {
    @InjectMocks
    private AddressService addressService;

    @Mock
    private LoadAddressPort loadAddressPort;

    @Mock
    private SaveAddressPort saveAddressPort;

    @Nested
    @DisplayName("주소 등록")
    class registerAddress {
        @Test
        @DisplayName("성공: 사용자의 첫 배송지 등록 시, 요청과 무관하게 기본 배송지로 설정된다.")
        void registerAddress_first_time_is_always_default() {
            // given
            RegisterAddressCommand command = RegisterAddressCommand.of(
                    1L, "김철수", "010-1234-5678", "06234",
                    "서울시 강남구", "101호", false // 요청은 false로 보냄
            );

            given(loadAddressPort.existsByMemberId(1L)).willReturn(false);

            // when
            addressService.registerAddress(command);

            // then
            verify(saveAddressPort).save(argThat(Address::isDefault));
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

            Address oldDefault = Address.builder()
                    .memberId(memberId).isDefault(true).build();

            given(loadAddressPort.existsByMemberId(memberId)).willReturn(true);
            given(loadAddressPort.findDefaultByMemberId(memberId)).willReturn(Optional.of(oldDefault));

            // when
            addressService.registerAddress(command);

            // then
            assertThat(oldDefault.isDefault()).isFalse(); // 기존 주소는 해제됨
            verify(saveAddressPort, times(2)).save(any(Address.class)); // 기존 수정 + 신규 저장
        }
    }

    @Nested
    @DisplayName("주소 수정")
    class updateAddress {
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

            Address existingAddress = Address.builder()
                    .id(addressId)
                    .memberId(memberId)
                    .isDefault(false)
                    .build();

            given(loadAddressPort.findById(addressId)).willReturn(Optional.of(existingAddress));

            // when
            addressService.updateAddress(command);

            // then
            assertThat(existingAddress.getRecipientName()).isEqualTo("홍길동");
            assertThat(existingAddress.getBaseAddress()).isEqualTo("경기도 성남시");
            verify(saveAddressPort).save(existingAddress);
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

            Address othersAddress = Address.builder()
                    .id(addressId)
                    .memberId(otherMemberId) // 소유자가 다름
                    .build();

            given(loadAddressPort.findById(addressId)).willReturn(Optional.of(othersAddress));

            // when & then
            assertThatThrownBy(() -> addressService.updateAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주소 ID로 수정 시 ADDRESS_NOT_FOUND 예외가 발생한다.")
        void updateAddress_fail_not_found() {
            // given
            Long addressId = 999L;
            UpdateAddressCommand command = new UpdateAddressCommand(
                    1L, addressId, "수정", "010-1111-2222", "12345", "주소", "상세", false
            );

            given(loadAddressPort.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.updateAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AddressErrorCode.ADDRESS_NOT_FOUND);
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

            Address targetAddress = Address.builder().id(addressId).memberId(memberId).isDefault(false).build();
            Address oldDefaultAddress = Address.builder().id(200L).memberId(memberId).isDefault(true).build();

            given(loadAddressPort.findById(addressId)).willReturn(Optional.of(targetAddress));
            given(loadAddressPort.findDefaultByMemberId(memberId)).willReturn(Optional.of(oldDefaultAddress));

            // when
            addressService.updateAddress(command);

            // then
            assertThat(targetAddress.isDefault()).isTrue();
            assertThat(oldDefaultAddress.isDefault()).isFalse(); // 기존 배송지는 해제
            verify(saveAddressPort, times(2)).save(any(Address.class));
        }
    }

    @Nested
    @DisplayName("주소 삭제")
    class deleteAddress {
        @Test
        @DisplayName("성공: 본인의 배송지를 삭제하면 정상적으로 삭제 처리된다.")
        void deleteAddress_success() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;

            Address existingAddress = Address.builder()
                    .id(addressId)
                    .memberId(memberId)
                    .isDefault(false)
                    .build();

            given(loadAddressPort.findById(addressId)).willReturn(Optional.of(existingAddress));

            // when
            addressService.deleteAddress(memberId, addressId);

            // then
            verify(saveAddressPort).delete(existingAddress);
        }

        @Test
        @DisplayName("성공: MVP 정책에 따라 기본 배송지도 삭제가 가능하다.")
        void deleteAddress_default_success() {
            // given
            Long memberId = 1L;
            Long addressId = 100L;

            Address defaultAddress = Address.builder()
                    .id(addressId)
                    .memberId(memberId)
                    .isDefault(true) // 기본 배송지
                    .build();

            given(loadAddressPort.findById(addressId)).willReturn(Optional.of(defaultAddress));

            // when
            addressService.deleteAddress(memberId, addressId);

            // then
            verify(saveAddressPort).delete(defaultAddress);
        }

        @Test
        @DisplayName("실패: 타인의 배송지를 삭제하려고 시도할 경우 FORBIDDEN_ADDRESS_ACCESS 예외가 발생한다.")
        void deleteAddress_fail_forbidden() {
            // given
            Long myId = 1L;
            Long otherMemberId = 2L;
            Long addressId = 100L;

            Address othersAddress = Address.builder()
                    .id(addressId)
                    .memberId(otherMemberId) // 소유자가 다름
                    .build();

            given(loadAddressPort.findById(addressId)).willReturn(Optional.of(othersAddress));

            // when & then
            assertThatThrownBy(() -> addressService.deleteAddress(myId, addressId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주소를 삭제하려고 시도할 경우 ADDRESS_NOT_FOUND 예외가 발생한다.")
        void deleteAddress_fail_not_found() {
            // given
            Long addressId = 999L;
            given(loadAddressPort.findById(addressId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> addressService.deleteAddress(1L, addressId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AddressErrorCode.ADDRESS_NOT_FOUND);
        }
    }


}