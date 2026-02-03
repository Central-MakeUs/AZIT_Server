package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.modules.member.application.port.in.AddressUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService implements AddressUseCase {
    private final LoadAddressPort loadAddressPort;
    private final SaveAddressPort saveAddressPort;

    @Override
    public void registerAddress(RegisterAddressCommand command) {
        // 주소가 이미 있는지 확인
        boolean hasExistingAddress = loadAddressPort.existsByMemberId(command.memberId());

        // 첫 주소 등록이거나 isDefault가 true면 기본 배송지로 설정
        boolean shouldBeDefault = !hasExistingAddress || command.isDefault();

        Address newAddress = Address.create(command.memberId(), command.recipientName(), command.phoneNumber(), command.zipcode(),
                command.baseAddress(), command.detailAddress(), shouldBeDefault);

        // 새 주소가 기본 배송지일 경우 기존의 기본 주소 해제
        if (shouldBeDefault && hasExistingAddress) {
            loadAddressPort.findDefaultByMemberId(command.memberId())
                    .ifPresent(oldDefault -> {
                        oldDefault.markAsNonDefault();
                        saveAddressPort.save(oldDefault);
                    });
        }

        saveAddressPort.save(newAddress);
    }
}
