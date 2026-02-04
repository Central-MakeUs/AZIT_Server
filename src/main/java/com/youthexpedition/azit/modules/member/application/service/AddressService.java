package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.AddressUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.AddressResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveAddressPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.AddressResponseMapper;
import com.youthexpedition.azit.modules.member.domain.model.Address;
import com.youthexpedition.azit.modules.member.domain.model.enums.AddressErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService implements AddressUseCase {
    private final LoadAddressPort loadAddressPort;
    private final SaveAddressPort saveAddressPort;
    private final AddressResponseMapper addressResponseMapper;

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
            resetExistingDefault(command.memberId());
        }

        saveAddressPort.save(newAddress);
    }

    @Override
    public void updateAddress(UpdateAddressCommand command) {
        Address address = getAddressValidated(command.addressId(), command.memberId());
        address.update(command.recipientName(), command.phoneNumber(), command.zipcode(), command.baseAddress(), command.detailAddress());

        // 기본 배송지 설정 확인
        if (command.isDefault() && !address.isDefault()) {
            resetExistingDefault(command.memberId());
            address.markAsDefault();
        }

        saveAddressPort.save(address);
    }

    @Override
    public void deleteAddress(Long memberId, Long addressId) {
        Address address = getAddressValidated(addressId, memberId);

        saveAddressPort.delete(address);
    }

    // 주소 조회 및 현재 로그인한 멤버의 소유인지 검증
    private Address getAddressValidated(Long addressId, Long memberId) {
        Address address = loadAddressPort.findById(addressId)
                .orElseThrow(() -> new BusinessException(AddressErrorCode.ADDRESS_NOT_FOUND));

        // 로그인한 멤버 주소지가 아닐 경우 권한 에러
        if (!address.getMemberId().equals(memberId)) {
            throw new BusinessException(AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }
        return address;
    }

    // 기존 기본 배송지를 일반 배송지로 변경
    private void resetExistingDefault(Long memberId) {
        loadAddressPort.findDefaultByMemberId(memberId)
                .ifPresent(oldDefault -> {
                    oldDefault.markAsNonDefault();
                    saveAddressPort.save(oldDefault);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long memberId) {
        List<Address> addresses = loadAddressPort.findAllByMemberIdOrderByDefault(memberId);

        return addressResponseMapper.toAddressResponseList(addresses);
    }
}
