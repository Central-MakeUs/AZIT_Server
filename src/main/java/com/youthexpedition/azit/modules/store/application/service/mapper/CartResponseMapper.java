package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.provider.ImageUrlProvider;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemListResponse;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartResponseMapper {

    private final ImageUrlProvider imageUrlProvider;

    public CartItemCountResponse toCountResponse(long count) {
        return CartItemCountResponse.from(count);
    }

    public List<CartItemListResponse> toCartItemListResponse(List<CartItemQueryDto> items) {
        return items.stream()
                .map(this::toItemDetail)
                .toList();
    }

    private CartItemListResponse toItemDetail(CartItemQueryDto cartItem) {
        return CartItemListResponse.of(
                cartItem.cartItemId(),
                cartItem.brandId(),
                cartItem.brandName(),
                cartItem.productId(),
                cartItem.productName(),
                Product.calculateExpectedShippingDate(cartItem.shippingLeadTime()),
                cartItem.skuId(),
                formatOptionValues(cartItem.optionValues()),
                imageUrlProvider.buildFullImageUrl(cartItem.imageUrl()),
                cartItem.basePrice() + cartItem.additionalPrice(),
                cartItem.salePrice() + cartItem.additionalPrice(),
                cartItem.quantity(),
                cartItem.stockQuantity() <= 0,
                cartItem.shippingFee()
        );
    }

    // 옵션 + · + 옵션 형식으로 조합하는 메서드
    private String formatOptionValues(List<String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }
        return String.join(" · ", optionValues);
    }

}
