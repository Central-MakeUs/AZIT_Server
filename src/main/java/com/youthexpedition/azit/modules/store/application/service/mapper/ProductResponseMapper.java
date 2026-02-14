package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import com.youthexpedition.azit.modules.store.domain.model.ProductImage;
import com.youthexpedition.azit.modules.store.domain.model.ProductOptionGroup;
import com.youthexpedition.azit.modules.store.domain.model.ProductOptionValue;
import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductResponseMapper {

    private final ImageUrlFormatUtil imageUrlFormatUtil;

    public ProductDetailResponse toDetailResponse(Product product) {
        return ProductDetailResponse.of(
                product,
                filterAndResolveUrls(product, ProductImageType.SLIDE),
                filterAndResolveUrls(product, ProductImageType.DETAIL),
                mapOptionGroups(product),
                mapSkus(product)
        );
    }

    private List<ProductDetailResponse.OptionGroupResponse> mapOptionGroups(Product product) {
        return product.getOptionGroups().stream()
                // 1. 옵션 그룹 정렬 (예: 컬러 -> 사이즈)
                .sorted(Comparator.comparing(ProductOptionGroup::getSortOrder))
                .map(group -> new ProductDetailResponse.OptionGroupResponse(
                        group.getId(),
                        group.getName(),
                        group.getValues().stream()
                                // 2. 옵션값 정렬 (예: S -> M -> L)
                                .sorted(Comparator.comparing(ProductOptionValue::getSortOrder))
                                .map(val -> {
                                    // ⭐️ 해당 옵션값을 포함하는 모든 SKU가 품절인지 판별
                                    boolean isSoldOut = product.getSkus().stream()
                                            .filter(sku -> sku.getSkuOptions().stream()
                                                    .anyMatch(opt -> opt.getOptionValue().getId().equals(val.getId())))
                                            .allMatch(sku -> sku.getStockQuantity() <= 0);

                                    return new ProductDetailResponse.OptionValueResponse(
                                            val.getId(),
                                            val.getValue(),
                                            isSoldOut
                                    );
                                })
                                .toList()
                )).toList();
    }

    private List<ProductDetailResponse.SkuResponse> mapSkus(Product product) {
        return product.getSkus().stream()
                .map(sku -> new ProductDetailResponse.SkuResponse(
                        sku.getId(),
                        sku.getAdditionalPrice(),
                        sku.getStockQuantity(),
                        sku.getSkuOptions().stream()
                                // 3. SKU 내부의 옵션 ID 리스트도 정렬된 순서로 제공
                                .sorted(Comparator.comparing(opt -> opt.getOptionValue().getSortOrder()))
                                .map(opt -> opt.getOptionValue().getId())
                                .toList(),
                        sku.getStockQuantity() <= 0
                )).toList();
    }

    public ProductListResponse toListResponse(Product product) {
        // 썸네일 이미지 추출
        List<String> slideImages = filterAndResolveUrls(product, ProductImageType.SLIDE);
        String thumbnailImageUrl = slideImages.isEmpty() ? null : slideImages.getFirst();

        return ProductListResponse.of(
                product.getId(),
                product.getBrand().getName(),
                product.getName(),
                product.getBasePrice(),
                product.getDiscountRate(),
                product.getSalePrice(),
                thumbnailImageUrl
        );
    }

    private List<String> filterAndResolveUrls(Product product, ProductImageType type) {
        return product.getImages().stream()
                .filter(img -> img.getImageType() == type)
                .sorted(Comparator.comparing(ProductImage::getSortOrder))
                .map(img -> imageUrlFormatUtil.buildFullImageUrl(img.getImageUrl()))
                .toList();
    }
}
