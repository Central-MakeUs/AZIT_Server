package com.youthexpedition.azit.modules.store.domain.model;

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
        this.quantity = newQuantity;
    }
}
