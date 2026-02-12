package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.DeliveryAddressResponse;

import java.util.List;

public interface DeliveryAddressUseCase {
    void registerDeliveryAddress(RegisterAddressCommand command);
    void updateDeliveryAddress(UpdateAddressCommand command);
    void deleteDeliveryAddress(Long memberId, Long addressId);
    List<DeliveryAddressResponse> getDeliveryAddresses(Long memberId);
}
