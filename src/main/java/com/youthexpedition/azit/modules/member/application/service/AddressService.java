package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.AddressUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.out.LoadAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveAddressPort;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import com.youthexpedition.azit.modules.member.domain.model.enums.AddressErrorCode;
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

    @Override
    public void updateAddress(UpdateAddressCommand command) {
        Address address = loadAddressPort.findById(command.addressId())
                .orElseThrow(() -> new BusinessException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 로그인한 멤버 주소지가 아닐 경우 권한 에러
        if (!address.getMemberId().equals(command.memberId())) {
            throw new BusinessException(AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }

        address.update(command.recipientName(), command.phoneNumber(), command.zipcode(), command.baseAddress(), command.detailAddress());

        // 기본 배송지 설정 처리
        // 기존에 기본이 아니었는데 기본으로 변경하려는 경우
        if (command.isDefault() && !address.isDefault()) {
            loadAddressPort.findDefaultByMemberId(command.memberId())
                    .ifPresent(oldDefault -> {
                        oldDefault.markAsNonDefault();
                        saveAddressPort.save(oldDefault);
                    });
            address.markAsDefault();
        }

        saveAddressPort.save(address);
    }

    @Override
    public void deleteAddress(Long memberId, Long addressId) {
        Address address = loadAddressPort.findById(addressId)
                .orElseThrow(() -> new BusinessException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 로그인한 멤버 주소지가 아닐 경우 권한 에러
        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }

        saveAddressPort.delete(address);
    }
}
