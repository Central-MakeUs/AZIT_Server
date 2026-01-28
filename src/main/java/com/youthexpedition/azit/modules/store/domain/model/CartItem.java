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

    // 장바구니 수량 변경
    public void changeQuantity(Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new BusinessException(StoreErrorCode.INVALID_QUANTITY);
        }
        this.quantity = newQuantity;
    }

    // 장바구니에 담긴 합계 금액 계산
    public Long calculateTotalPrice() {
        return (product.getSalePrice() + sku.getAdditionalPrice()) * quantity;
    }
}
