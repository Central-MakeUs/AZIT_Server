package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.infrastructure.common.util.image.ImageUrlFormatUtil;
import com.youthexpedition.azit.infrastructure.common.util.StringFormatUtil;
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

    private final ImageUrlFormatUtil imageUrlFormatUtil;

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
                StringFormatUtil.formatOptionValues(cartItem.optionValues()),
                imageUrlFormatUtil.buildFullImageUrl(cartItem.imageUrl()),
                cartItem.basePrice() + cartItem.additionalPrice(),
                cartItem.salePrice() + cartItem.additionalPrice(),
                cartItem.quantity(),
                cartItem.stockQuantity() <= 0,
                cartItem.shippingFee()
        );
    }

}
