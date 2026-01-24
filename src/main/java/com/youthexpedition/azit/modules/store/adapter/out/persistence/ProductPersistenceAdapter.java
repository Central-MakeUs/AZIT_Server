package com.youthexpedition.azit.modules.store.adapter.out.persistence;

import com.youthexpedition.azit.infrastructure.common.response.SliceResponse;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper.ProductMapper;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.repository.ProductRepository;
import com.youthexpedition.azit.modules.store.application.port.in.dto.ProductListResponse;
import com.youthexpedition.azit.modules.store.application.port.in.query.GetProductListQuery;
import com.youthexpedition.azit.modules.store.application.port.out.LoadProductPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements LoadProductPort, SaveProductPort {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Override
    public SliceResponse<ProductListResponse> findProducts(GetProductListQuery query) {
        SliceResponse<ProductListResponse> response = productRepository.findProducts(query);

        List<ProductListResponse> content = response.content().stream()
                .map(original -> productMapper.toListResponse(original, cloudFrontDomain))
                .toList();

        return new SliceResponse<>(content, response.hasNext(), response.lastId());
    }
}
