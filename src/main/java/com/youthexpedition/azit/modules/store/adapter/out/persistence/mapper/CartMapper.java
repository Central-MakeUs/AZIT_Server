package com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.CartItemEntity;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductEntity;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductSkuEntity;
import com.youthexpedition.azit.modules.store.domain.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final ProductMapper productMapper;

    public CartItem toDomain(CartItemEntity entity) {
        if (entity == null) return null;

        return CartItem.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .product(productMapper.toDomainForCart(entity.getProduct()))
                .sku(productMapper.toSkuDomainForCart(entity.getSku()))
                .quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CartItemEntity toEntity(CartItem domain) {
        if (domain == null) return null;

        return CartItemEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .product(ProductEntity.builder().id(domain.getProduct().getId()).build())
                .sku(ProductSkuEntity.builder().id(domain.getSku().getId()).build())
                .quantity(domain.getQuantity())
                .build();
    }
}