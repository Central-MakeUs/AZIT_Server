package com.youthexpedition.azit.modules.store.application.port.out;

public interface SaveProductPort {
    void decreaseStock(Long skuId, int quantity);
}
