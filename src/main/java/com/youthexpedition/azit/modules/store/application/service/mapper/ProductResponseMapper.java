package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.ImageUrlFormatUtil;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.domain.model.*;
import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

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
        // 옵션값 ID를 키로, 해당 옵션을 포함하는 SKU 리스트를 값으로 갖는 Map 생성
        Map<Long, List<ProductSku>> skuMapByOptionValue = new HashMap<>();
        for (ProductSku sku : product.getSkus()) {
            for (ProductSkuOption option : sku.getSkuOptions()) {
                skuMapByOptionValue
                        .computeIfAbsent(option.getOptionValue().getId(), k -> new ArrayList<>())
                        .add(sku);
            }
        }

        return product.getOptionGroups().stream()
                .sorted(Comparator.comparing(ProductOptionGroup::getSortOrder))
                .map(group -> new ProductDetailResponse.OptionGroupResponse(
                        group.getId(),
                        group.getName(),
                        group.getValues().stream()
                                .sorted(Comparator.comparing(ProductOptionValue::getSortOrder))
                                .map(val -> {
                                    // Map에서 해당 옵션값이 포함된 SKU들만 즉시 조회
                                    List<ProductSku> associatedSkus = skuMapByOptionValue.getOrDefault(val.getId(), List.of());

                                    // 연관된 모든 SKU의 재고가 0 이하일 때만 품절 처리
                                    boolean isSoldOut = !associatedSkus.isEmpty() &&
                                            associatedSkus.stream().allMatch(sku -> sku.getStockQuantity() <= 0);

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
