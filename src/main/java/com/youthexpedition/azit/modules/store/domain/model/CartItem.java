package com.youthexpedition.azit.modules.store.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CartItem {
    private final Long id;
    private final Long memberId;
    private final Product product;
    private final ProductSku sku;
    private Integer quantity;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static CartItem create(Long memberId, Product product, ProductSku sku, int quantity) {
        return CartItem.builder()
                .memberId(memberId)
                .product(product)
                .sku(sku)
                .quantity(quantity)
                .build();
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new BusinessException(StoreErrorCode.INVALID_QUANTITY); // 수량은 1개 이상이어야 함
        }
        this.quantity = newQuantity;
    }
}
