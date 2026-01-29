package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QBrandEntity.brandEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QCartItemEntity.cartItemEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductEntity.productEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductImageEntity.productImageEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductOptionValueEntity.productOptionValueEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductSkuEntity.productSkuEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductSkuOptionEntity.productSkuOptionEntity;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartItemQueryDto> findCartDetailsByMemberId(Long memberId) {

        // 메인 정보 조회
        List<CartItemQueryDto> results = queryFactory
                .select(Projections.constructor(CartItemQueryDto.class,
                        cartItemEntity.id,
                        brandEntity.name,
                        productEntity.name,
                        productEntity.shippingLeadTime,
                        JPAExpressions
                                .select(productImageEntity.imageUrl)
                                .from(productImageEntity)
                                .where(productImageEntity.product.eq(productEntity)
                                        .and(productImageEntity.imageType.eq(ProductImageType.SLIDE))
                                        .and(productImageEntity.sortOrder.eq(1)))
                                .limit(1),
                        productEntity.basePrice,
                        productEntity.salePrice,
                        productSkuEntity.additionalPrice,
                        cartItemEntity.quantity,
                        productSkuEntity.stockQuantity,
                        brandEntity.id,
                        productEntity.shippingFee,
                        com.querydsl.core.types.dsl.Expressions.constant(Collections.emptyList()) // 임시 빈 리스트
                ))
                .from(cartItemEntity)
                .join(cartItemEntity.product, productEntity)
                .join(productEntity.brand, brandEntity)
                .join(cartItemEntity.sku, productSkuEntity)
                .where(cartItemEntity.memberId.eq(memberId))
                .fetch();

        if (results.isEmpty()) return results;

        // 장바구니 아이템들의 모든 옵션값을 한 번에 조회
        List<Long> cartItemIds = results.stream().map(CartItemQueryDto::cartItemId).toList();

        List<Tuple> options = queryFactory
                .select(cartItemEntity.id, productOptionValueEntity.value)
                .from(cartItemEntity)
                .join(cartItemEntity.sku, productSkuEntity)
                .join(productSkuEntity.skuOptions, productSkuOptionEntity)
                .join(productSkuOptionEntity.optionValue, productOptionValueEntity)
                .where(cartItemEntity.id.in(cartItemIds))
                .fetch();

        // 아이디별로 옵션 리스트 그룹화
        Map<Long, List<String>> optionsMap = options.stream()
                .collect(Collectors.groupingBy(
                        t -> Objects.requireNonNull(t.get(cartItemEntity.id)),
                        Collectors.mapping(t -> t.get(productOptionValueEntity.value), Collectors.toList())
                ));

        // 메인 정보와 옵션 리스트 병합
        return results.stream()
                .map(dto -> new CartItemQueryDto(
                        dto.cartItemId(), dto.brandName(), dto.productName(), dto.shippingLeadTime(),
                        dto.imageUrl(), dto.basePrice(), dto.salePrice(), dto.additionalPrice(),
                        dto.quantity(), dto.stockQuantity(), dto.brandId(), dto.shippingFee(),
                        optionsMap.getOrDefault(dto.cartItemId(), List.of())
                ))
                .toList();
    }
}
