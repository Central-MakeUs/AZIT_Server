package com.youthexpedition.azit.modules.member.application.port.out;

import com.youthexpedition.azit.modules.member.domain.model.DeliveryAddress;

public interface SaveDeliveryAddressPort {
    void save(DeliveryAddress deliveryAddress);
    void delete(DeliveryAddress deliveryAddress);
}
