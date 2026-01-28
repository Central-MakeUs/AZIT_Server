package com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.*;
import com.youthexpedition.azit.modules.store.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public Product toDomain(ProductEntity entity) {
        if (entity == null) return null;

        return Product.builder()
                .id(entity.getId())
                .brand(toBrandDomain(entity.getBrand()))
                .name(entity.getName())
                .basePrice(entity.getBasePrice())
                .discountRate(entity.getDiscountRate())
                .salePrice(entity.getSalePrice())
                .shippingFee(entity.getShippingFee())
                .shippingLeadTime(entity.getShippingLeadTime())
                .refundPolicy(entity.getRefundPolicy())
                .description(entity.getDescription())
                .images(entity.getImages().stream()
                        .map(this::toImageDomain)
                        .collect(Collectors.toList()))
                .optionGroups(entity.getOptionGroups().stream()
                        .map(this::toOptionGroupDomain)
                        .collect(Collectors.toList()))
                .skus(entity.getSkus().stream()
                        .map(this::toSkuDomain)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private ProductImage toImageDomain(ProductImageEntity entity) {
        if (entity == null) return null;

        return ProductImage.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .sortOrder(entity.getSortOrder())
                .imageType(entity.getImageType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private ProductOptionGroup toOptionGroupDomain(ProductOptionGroupEntity entity) {
        if (entity == null) return null;

        return ProductOptionGroup.builder()
                .id(entity.getId())
                .name(entity.getName())
                .sortOrder(entity.getSortOrder())
                .values(entity.getValues().stream()
                        .map(this::toOptionValueDomain)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private ProductOptionValue toOptionValueDomain(ProductOptionValueEntity entity) {
        if (entity == null) return null;

        return ProductOptionValue.builder()
                .id(entity.getId())
                .value(entity.getValue())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public ProductSku toSkuDomain(ProductSkuEntity entity) {
        if (entity == null) return null;

        return ProductSku.builder()
                .id(entity.getId())
                .additionalPrice(entity.getAdditionalPrice())
                .stockQuantity(entity.getStockQuantity())
                .skuOptions(entity.getSkuOptions().stream()
                        .map(this::toSkuOptionDomain)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private ProductSkuOption toSkuOptionDomain(ProductSkuOptionEntity entity) {
        if (entity == null) return null;

        return ProductSkuOption.builder()
                .id(entity.getId())
                .optionValue(toOptionValueDomain(entity.getOptionValue()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private Brand toBrandDomain(BrandEntity entity) {
        if (entity == null) return null;

        return Brand.builder()
                .id(entity.getId())
                .name(entity.getName())
                .logoImageUrl(entity.getLogoImageUrl())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public ProductEntity toEntity(Product domain) {
        if (domain == null) return null;

        return ProductEntity.builder()
                .id(domain.getId())
                .brand(BrandEntity.builder().id(domain.getBrand().getId()).build())
                .name(domain.getName())
                .basePrice(domain.getBasePrice())
                .discountRate(domain.getDiscountRate())
                .salePrice(domain.getSalePrice())
                .shippingFee(domain.getShippingFee())
                .shippingLeadTime(domain.getShippingLeadTime())
                .refundPolicy(domain.getRefundPolicy())
                .description(domain.getDescription())
                .build();
    }

    public ProductSkuEntity toSkuEntity(ProductSku domain) {
        if (domain == null) return null;

        return ProductSkuEntity.builder()
                .id(domain.getId())
                .additionalPrice(domain.getAdditionalPrice())
                .stockQuantity(domain.getStockQuantity())
                .build();
    }

    // 장바구니 전용 변환 메서드
    public Product toDomainForCart(ProductEntity entity) {
        if (entity == null) return null;

        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .basePrice(entity.getBasePrice())
                .discountRate(entity.getDiscountRate())
                .salePrice(entity.getSalePrice())
                .shippingFee(entity.getShippingFee())
                .build();
    }

    // SKU 정보 중 장바구니(재고/금액 확인)에 필요한 최소 정보만 매핑
    public ProductSku toSkuDomainForCart(ProductSkuEntity entity) {
        if (entity == null) return null;

        return ProductSku.builder()
                .id(entity.getId())
                .additionalPrice(entity.getAdditionalPrice())
                .stockQuantity(entity.getStockQuantity())
                .build();
    }
}
