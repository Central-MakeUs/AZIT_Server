package com.youthexpedition.azit.modules.store.application.port.out.query;

public interface PriceCalculateDto {
    Long brandId();
    Long shippingFee();
    Long basePrice();
    Long salePrice();
    Long additionalPrice();
    Integer quantity();

}
