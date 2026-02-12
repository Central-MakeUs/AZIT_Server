package com.youthexpedition.azit.modules.store.application.port.out;

import com.youthexpedition.azit.modules.store.domain.model.Order;

public interface SaveOrderPort {
    Order save(Order order);
}
