package com.youthexpedition.azit.modules.store.adapter.out.persistence;

import com.youthexpedition.azit.infrastructure.common.query.CursorPageQuery;
import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductEntity;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper.ProductMapper;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.repository.ProductRepository;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.repository.ProductSkuRepository;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.out.LoadProductPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveProductPort;
import com.youthexpedition.azit.modules.store.application.port.out.query.CheckoutItemDto;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements LoadProductPort, SaveProductPort {
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductMapper productMapper;

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Override
    public SliceResponse<Product> findProducts(CursorPageQuery query) {
        SliceResponse<ProductEntity> entitySlice = productRepository.findProducts(query);

        List<Product> content = entitySlice.content().stream()
                .map(productMapper::toDomain)
                .toList();

        return new SliceResponse<>(content, entitySlice.hasNext(), entitySlice.lastId());
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productRepository.findByIdWithAllDetails(productId)
                .map(productMapper::toDomain);
    }

    private ProductListResponse resolveImageUrl(ProductListResponse dto) {
        return ProductListResponse.of(dto.id(), dto.brandName(), dto.productName(), dto.basePrice(), dto.discountRate(), dto.salePrice(),
                buildFullImageUrl(dto.thumbnailImageUrl())
        );
    }

    private String buildFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        return cloudFrontDomain + imagePath;
    }

    @Override
    public Optional<Product> findByIdForCart(Long productId) {
        return productRepository.findByIdForCart(productId)
                .map(productMapper::toDomain);
    }

    @Override
    public void decreaseStock(Long skuId, int quantity) {
        int updatedCount = productSkuRepository.decreaseStock(skuId, quantity);
        if (updatedCount == 0) {
            // 업데이트된 행이 0개일 경우: 재고가 부족하거나 SKU가 없음
            throw new BusinessException(StoreErrorCode.OUT_OF_STOCK);
        }
    }

    @Override
    public Optional<CheckoutItemDto> findProductInfoBySkuId(Long skuId, int quantity) {
        return productRepository.findProductInfoBySkuId(skuId, quantity);
    }
}
