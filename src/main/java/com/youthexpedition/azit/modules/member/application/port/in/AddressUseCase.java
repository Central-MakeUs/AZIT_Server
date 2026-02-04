package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.dto.AddressResponse;

import java.util.List;

public interface AddressUseCase {
    void registerAddress(RegisterAddressCommand command);
    void updateAddress(UpdateAddressCommand command);
    void deleteAddress(Long memberId, Long addressId);
    List<AddressResponse> getAddresses(Long memberId);
}
