package com.youthexpedition.azit.modules.member.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.application.port.in.DeliveryAddressUseCase;
import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;
import com.youthexpedition.azit.modules.member.application.port.out.LoadDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.port.out.SaveDeliveryAddressPort;
import com.youthexpedition.azit.modules.member.application.service.mapper.DeliveryAddressResponseMapper;
import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;
import com.youthexpedition.azit.modules.member.domain.model.enums.DeliveryAddressErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryAddressService implements DeliveryAddressUseCase {
    private final LoadDeliveryAddressPort loadDeliveryAddressPort;
    private final SaveDeliveryAddressPort saveDeliveryAddressPort;
    private final DeliveryAddressResponseMapper deliveryAddressResponseMapper;

    @Override
    public void registerDeliveryAddress(RegisterAddressCommand command) {
        // 주소가 이미 있는지 확인
        boolean hasExistingAddress = loadDeliveryAddressPort.existsByMemberId(command.memberId());

        // 첫 주소 등록이거나 isDefault가 true면 기본 배송지로 설정
        boolean shouldBeDefault = !hasExistingAddress || command.isDefault();

        DeliveryAddress newDeliveryAddress = DeliveryAddress.create(command.memberId(), command.recipientName(), command.phoneNumber(), command.zipcode(),
                command.baseAddress(), command.detailAddress(), shouldBeDefault);

        // 새 주소가 기본 배송지일 경우 기존의 기본 주소 해제
        if (shouldBeDefault && hasExistingAddress) {
            resetExistingDefault(command.memberId());
        }

        saveDeliveryAddressPort.save(newDeliveryAddress);
    }

    @Override
    public void updateDeliveryAddress(UpdateAddressCommand command) {
        DeliveryAddress deliveryAddress = getAddressValidated(command.addressId(), command.memberId());
        deliveryAddress.update(command.recipientName(), command.phoneNumber(), command.zipcode(), command.baseAddress(), command.detailAddress());

        // 기본 배송지 설정 확인
        if (command.isDefault() && !deliveryAddress.isDefault()) {
            resetExistingDefault(command.memberId());
            deliveryAddress.markAsDefault();
        }

        saveDeliveryAddressPort.save(deliveryAddress);
    }

    @Override
    public void deleteDeliveryAddress(Long memberId, Long addressId) {
        DeliveryAddress deliveryAddress = getAddressValidated(addressId, memberId);

        saveDeliveryAddressPort.delete(deliveryAddress);
    }

    // 주소 조회 및 현재 로그인한 멤버의 소유인지 검증
    private DeliveryAddress getAddressValidated(Long addressId, Long memberId) {
        DeliveryAddress deliveryAddress = loadDeliveryAddressPort.findById(addressId)
                .orElseThrow(() -> new BusinessException(DeliveryAddressErrorCode.ADDRESS_NOT_FOUND));

        // 로그인한 멤버 주소지가 아닐 경우 권한 에러
        if (!deliveryAddress.getMemberId().equals(memberId)) {
            throw new BusinessException(DeliveryAddressErrorCode.FORBIDDEN_ADDRESS_ACCESS);
        }
        return deliveryAddress;
    }

    // 기존 기본 배송지를 일반 배송지로 변경
    private void resetExistingDefault(Long memberId) {
        loadDeliveryAddressPort.findDefaultByMemberId(memberId)
                .ifPresent(oldDefault -> {
                    oldDefault.markAsNonDefault();
                    saveDeliveryAddressPort.save(oldDefault);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryAddressResponse> getDeliveryAddresses(Long memberId) {
        List<DeliveryAddress> deliveryAddresses = loadDeliveryAddressPort.findAllByMemberIdOrderByDefault(memberId);

        return deliveryAddressResponseMapper.toAddressResponseList(deliveryAddresses);
    }
}
