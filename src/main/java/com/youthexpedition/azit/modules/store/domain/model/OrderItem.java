package com.youthexpedition.azit.modules.store.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderItem {
    private final Long id;
    private final Long productId;
    private final Long skuId;
    private final String productName;
    private final String optionDescription;
    private final long price;
    private final int quantity;

    public static OrderItem create(Long productId, Long skuId, String productName, String optionDescription, long price, int quantity) {
        validateQuantity(quantity);

        return OrderItem.builder()
                .productId(productId)
                .skuId(skuId)
                .productName(productName)
                .optionDescription(optionDescription)
                .price(price)
                .quantity(quantity)
                .build();
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(StoreErrorCode.INVALID_QUANTITY);
        }
    }
}
