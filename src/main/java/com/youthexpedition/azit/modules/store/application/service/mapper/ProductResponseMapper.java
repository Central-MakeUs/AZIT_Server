package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductDetailResponse;
import com.youthexpedition.azit.modules.store.domain.model.*;
import com.youthexpedition.azit.modules.store.domain.model.enums.ProductImageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProductResponseMapper {

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public ProductDetailResponse toDetailResponse(Product product) {
        return ProductDetailResponse.of(
                product,
                filterAndResolveUrls(product, ProductImageType.SLIDE),
                filterAndResolveUrls(product, ProductImageType.DETAIL),
                mapOptionGroups(product),
                mapSkus(product)
        );
    }

    private List<String> filterAndResolveUrls(Product product, ProductImageType type) {
        return product.getImages().stream()
                .filter(img -> img.getImageType() == type)
                .sorted(Comparator.comparing(ProductImage::getSortOrder))
                .map(img -> cloudFrontDomain + img.getImageUrl())
                .toList();
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
                        sku.getSkuOptions().stream().map(opt -> opt.getOptionValue().getId()).toList()
                )).toList();
    }
}
