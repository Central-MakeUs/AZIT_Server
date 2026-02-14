package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductEntity;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QBrandEntity.brandEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductEntity.productEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductImageEntity.productImageEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductOptionValueEntity.productOptionValueEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductSkuEntity.productSkuEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductSkuOptionEntity.productSkuOptionEntity;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<ProductEntity> findProducts(CursorPageQuery query) {
        // hasNext 판단을 위해 size + 1 개 조회
        List<ProductEntity> content = queryFactory
                .selectFrom(productEntity)
                .join(productEntity.brand, brandEntity).fetchJoin() // Brand 정보 한 번에 조회
                .where(
                        ltCursorId(query.cursorId()),
                        isProductInStock())
                .orderBy(productEntity.id.desc())
                .limit((long) query.size() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > query.size()) {
            hasNext = true;
            content.remove(query.size());
        }

        Long lastId = content.isEmpty() ? null : content.getLast().getId();

        return new SliceResponse<>(content, hasNext, lastId);
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId == null ? null : productEntity.id.lt(cursorId);
    }

    @Override
    public Optional<ProductEntity> findByIdWithAllDetails(Long productId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(productEntity)
                .join(productEntity.brand, brandEntity).fetchJoin()
                .leftJoin(productEntity.images, productImageEntity).fetchJoin()
                .where(productEntity.id.eq(productId))
                .fetchOne());
    }

    @Override
    public Optional<ProductEntity> findByIdForCart(Long productId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(productEntity)
                // 재고 확인을 위한 skus만 fetch 조인
                .leftJoin(productEntity.skus, productSkuEntity).fetchJoin()
                .where(productEntity.id.eq(productId))
                .fetchOne());
    }

    @Override
    public Optional<CheckoutItemDto> findProductInfoBySkuId(Long skuId, int quantity) {
        // 메인 정보 조회 (브랜드, 상품, SKU 정보 포함)
        Tuple tuple = queryFactory
                .select(
                        productEntity.id,
                        productSkuEntity.id,
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
                        productSkuEntity.stockQuantity,
                        brandEntity.id,
                        productEntity.shippingFee
                )
                .from(productSkuEntity)
                .join(productSkuEntity.product, productEntity)
                .join(productEntity.brand, brandEntity)
                .where(productSkuEntity.id.eq(skuId))
                .fetchOne();

        if (tuple == null) return Optional.empty();

        // 옵션 정보 조회
        List<String> optionValues = queryFactory
                .select(productOptionValueEntity.value)
                .from(productSkuOptionEntity)
                .join(productSkuOptionEntity.optionValue, productOptionValueEntity)
                .where(productSkuOptionEntity.productSku.id.eq(skuId))
                .fetch();

        // 3. DTO 매핑 (quantity는 파라미터로 받은 값 사용)
        return Optional.of(new CheckoutItemDto(
                tuple.get(productEntity.id),
                tuple.get(productSkuEntity.id),
                tuple.get(brandEntity.name),
                tuple.get(productEntity.name),
                tuple.get(productEntity.shippingLeadTime),
                tuple.get(5, String.class),
                tuple.get(productEntity.basePrice),
                tuple.get(productEntity.salePrice),
                tuple.get(productSkuEntity.additionalPrice),
                quantity,
                tuple.get(productSkuEntity.stockQuantity),
                tuple.get(brandEntity.id),
                tuple.get(productEntity.shippingFee),
                optionValues
        ));
    }

    // 상품에 재고가 있는지 확인
    private BooleanExpression isProductInStock() {
        return JPAExpressions
                .selectOne()
                .from(productSkuEntity)
                .where(productSkuEntity.product.eq(productEntity)
                        .and(productSkuEntity.stockQuantity.gt(0)))
                .exists();
    }
}
