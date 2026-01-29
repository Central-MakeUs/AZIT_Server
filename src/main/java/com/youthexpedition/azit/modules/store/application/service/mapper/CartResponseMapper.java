package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartListResponse;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartResponseMapper {
    public CartItemCountResponse toCountResponse(long count) {
        return CartItemCountResponse.from(count);
    }

    public CartListResponse toCartListResponse(List<CartItemQueryDto> items, long totalProductPrice, long totalMembershipDiscount, long totalShippingFee) {
        List<CartListResponse.CartItemDetail> details = items.stream()
                .map(this::toItemDetail)
                .toList();

        return CartListResponse.of(details, totalProductPrice, totalMembershipDiscount, totalShippingFee);
    }

    private CartListResponse.CartItemDetail toItemDetail(CartItemQueryDto cartItem) {
        return new CartListResponse.CartItemDetail(
                cartItem.cartItemId(),
                cartItem.brandName(),
                cartItem.productName(),
                Product.calculateExpectedShippingDate(cartItem.shippingLeadTime()),
                formatOptionValues(cartItem.optionValues()),
                cartItem.imageUrl(),
                cartItem.basePrice() + cartItem.additionalPrice(),
                cartItem.salePrice() + cartItem.additionalPrice(),
                cartItem.quantity(),
                cartItem.stockQuantity() <= 0
        );
    }

    // 옵션 + / + 옵션 형식으로 조합하는 메서드
    private String formatOptionValues(List<String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }
        return String.join(" / ", optionValues);
    }
}
