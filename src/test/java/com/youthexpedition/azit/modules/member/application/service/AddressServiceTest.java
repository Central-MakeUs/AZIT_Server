package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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