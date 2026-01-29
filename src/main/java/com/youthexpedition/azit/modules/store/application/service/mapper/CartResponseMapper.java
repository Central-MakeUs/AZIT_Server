package com.youthexpedition.azit.modules.store.application.service.mapper;

import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import org.springframework.stereotype.Component;

@Component
public class CartResponseMapper {
    public CartItemCountResponse toCountResponse(long count) {
        return CartItemCountResponse.from(count);
    }
}
