package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;
import com.youthexpedition.azit.modules.member.application.port.in.command.UpdateAddressCommand;

public interface AddressUseCase {
    void registerAddress(RegisterAddressCommand command);
    void updateAddress(UpdateAddressCommand command);
}
