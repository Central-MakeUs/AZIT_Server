package com.youthexpedition.azit.modules.store.application.port.out;

public interface LoadOrderPort {
    boolean existsByOrderNumber(String orderNumber);
}
