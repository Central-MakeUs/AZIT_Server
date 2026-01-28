package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductEntity;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;
import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QBrandEntity.brandEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductEntity.productEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductImageEntity.productImageEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductOptionGroupEntity.productOptionGroupEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductOptionValueEntity.productOptionValueEntity;
import static com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.QProductSkuEntity.productSkuEntity;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<ProductListResponse> findProducts(GetProductListQuery query) {
        // hasNext 판단을 위해 size + 1 개 조회
        List<ProductListResponse> content = queryFactory
                .select(Projections.constructor(ProductListResponse.class,
                        productEntity.id,
                        brandEntity.name,
                        productEntity.name,
                        productEntity.basePrice,
                        productEntity.discountRate,
                        productEntity.salePrice,
                        productImageEntity.imageUrl
                ))
                .from(productEntity)
                .join(productEntity.brand, brandEntity) // brand가 없는 상품은 없으므로 inner join
                .leftJoin(productEntity.images, productImageEntity).on(
                        productImageEntity.sortOrder.eq(1) // 노출 순서가 가장 먼저인 이미지
                        .and(productImageEntity.imageType.eq(ProductImageType.SLIDE))
                )
                .where(ltCursorId(query.cursorId()))
                .orderBy(productEntity.id.desc()) // 최신순 정렬
                .limit(query.size() + 1)
                .fetch();

        boolean hasNext = content.size() > query.size();
        if (hasNext) {
            content.remove(query.size());
        }

        Long lastId = content.isEmpty() ? null : content.getLast().id();

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
}
