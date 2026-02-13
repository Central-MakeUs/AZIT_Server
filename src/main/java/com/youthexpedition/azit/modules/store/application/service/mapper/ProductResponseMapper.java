package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.infrastructure.provider.ImageUrlProvider;
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

    private final ImageUrlProvider imageUrlProvider;

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
                .sorted(Comparator.comparing(ProductOptionGroup::getSortOrder)) // 그룹 정렬
                .map(og -> new ProductDetailResponse.OptionGroupResponse(
                        og.getId(),
                        og.getName(),
                        og.getValues().stream()
                                .sorted(Comparator.comparing(ProductOptionValue::getSortOrder)) // 노출 순서로 정렬
                                .map(v -> new ProductDetailResponse.OptionValueResponse(v.getId(), v.getValue()))
                                .toList()
                )).toList();
    }

    private List<ProductDetailResponse.SkuResponse> mapSkus(Product product) {
        return product.getSkus().stream()
                .map(sku -> new ProductDetailResponse.SkuResponse(
                        sku.getId(), sku.getAdditionalPrice(), sku.getStockQuantity(),
                        sku.getSkuOptions().stream()
                                .sorted(Comparator.comparing(opt -> opt.getOptionValue().getSortOrder()))
                                .map(opt -> opt.getOptionValue().getId()).toList()
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
                .map(img -> imageUrlProvider.buildFullImageUrl(img.getImageUrl()))
                .toList();
    }
}
