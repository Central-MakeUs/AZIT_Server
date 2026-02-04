package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.Address;

public interface SaveAddressPort {
    void save(Address address);
    void delete(Address address);
}
