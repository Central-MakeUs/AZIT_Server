package com.youthexpedition.azit.modules.member.application.port.in;

import com.youthexpedition.azit.modules.member.application.port.in.command.RegisterAddressCommand;

public interface AddressUseCase {
    void registerAddress(RegisterAddressCommand command);
}
