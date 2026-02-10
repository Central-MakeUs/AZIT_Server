package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QCartItemEntity;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
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
        return fetchCartDetails(cartItemEntity.memberId.eq(memberId));
    }

    @Override
public List<CheckoutItemDto> findCartDetailsByIds(List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return Collections.emptyList();
        }
        return fetchCartDetails(cartItemEntity.id.in(cartItemIds)).stream()
                .map(it -> new CheckoutItemDto(
                        it.productId(),
                        it.skuId(),
                        it.brandName(),
                        it.productName(),
                        it.shippingLeadTime(),
                        it.imageUrl(),
                        it.basePrice(),
                        it.salePrice(),
                        it.additionalPrice(),
                        it.quantity(),
                        it.stockQuantity(),
                        it.brandId(),
                        it.shippingFee(),
                        it.optionValues()
                )).toList();
    }


    private List<CartItemQueryDto> fetchCartDetails(BooleanExpression condition) {
        // 서브쿼리에서 사용할 별칭
        QCartItemEntity subCartItem = new QCartItemEntity("subCartItem");
        StringPath thumbnailImageUrl = Expressions.stringPath("thumbnailImageUrl");

        // 메인 정보 조회 (브랜드, 상품, SKU 정보 포함)
        List<Tuple> mainTuples = queryFactory
                .select(
                        cartItemEntity.id.as("car"),
                        productEntity.id,
                        productSkuEntity.id,
                        brandEntity.name,
                        productEntity.name,
                        productEntity.shippingLeadTime,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(productImageEntity.imageUrl)
                                        .from(productImageEntity)
                                        .where(productImageEntity.product.eq(productEntity)
                                                .and(productImageEntity.imageType.eq(ProductImageType.SLIDE))
                                                .and(productImageEntity.sortOrder.eq(1)))
                                        .limit(1),
                                thumbnailImageUrl
                        ),
                        productEntity.basePrice,
                        productEntity.salePrice,
                        productSkuEntity.additionalPrice,
                        cartItemEntity.quantity,
                        productSkuEntity.stockQuantity,
                        brandEntity.id,
                        productEntity.shippingFee
                )
                .from(cartItemEntity)
                .join(cartItemEntity.product, productEntity)
                .join(productEntity.brand, brandEntity)
                .join(cartItemEntity.sku, productSkuEntity)
                .where(condition)
                .orderBy(
                        // 가장 최근에 담은 브랜드 순 정렬
                        Expressions.asNumber(
                                JPAExpressions
                                        .select(subCartItem.id.max())
                                        .from(subCartItem)
                                        .where(subCartItem.product.brand.id.eq(brandEntity.id)
                                                .and(subCartItem.memberId.eq(cartItemEntity.memberId)))
                        ).desc(),
                        // 브랜드 내 개별 아이템 최신순 정렬
                        cartItemEntity.id.desc()
                )
                .fetch();

        if (mainTuples.isEmpty()) return Collections.emptyList();

        // 옵션 정보 조회
        List<Long> cartItemIds = mainTuples.stream()
                .map(t -> t.get(cartItemEntity.id))
                .toList();

        List<Tuple> optionTuples = queryFactory
                .select(cartItemEntity.id, productOptionValueEntity.value)
                .from(cartItemEntity)
                .join(cartItemEntity.sku, productSkuEntity)
                .join(productSkuEntity.skuOptions, productSkuOptionEntity)
                .join(productSkuOptionEntity.optionValue, productOptionValueEntity)
                .where(cartItemEntity.id.in(cartItemIds))
                .fetch();

        // 옵션 그룹화
        Map<Long, List<String>> optionsMap = optionTuples.stream()
                .collect(Collectors.groupingBy(
                        t -> Objects.requireNonNull(t.get(cartItemEntity.id)),
                        Collectors.mapping(t -> t.get(productOptionValueEntity.value), Collectors.toList())
                ));

        // 최종 DTO 생성
        return mainTuples.stream()
                .map(t -> new CartItemQueryDto(
                        t.get(cartItemEntity.id),
                        t.get(productEntity.id),
                        t.get(productSkuEntity.id),
                        t.get(brandEntity.name),
                        t.get(productEntity.name),
                        t.get(productEntity.shippingLeadTime),
                        t.get(thumbnailImageUrl),
                        t.get(productEntity.basePrice),
                        t.get(productEntity.salePrice),
                        t.get(productSkuEntity.additionalPrice),
                        t.get(cartItemEntity.quantity),
                        t.get(productSkuEntity.stockQuantity),
                        t.get(brandEntity.id),
                        t.get(productEntity.shippingFee),
                        optionsMap.getOrDefault(t.get(cartItemEntity.id), List.of())
                ))
                .toList();
    }

}
