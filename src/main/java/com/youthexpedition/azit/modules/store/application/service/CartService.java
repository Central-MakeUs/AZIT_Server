package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.store.application.port.in.CartUseCase;
import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;
import com.youthexpedition.azit.modules.store.application.port.in.command.CartItemDeleteCommand;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartItemCountResponse;
import com.youthexpedition.azit.modules.store.application.port.in.dto.CartListResponse;
import com.youthexpedition.azit.modules.store.application.port.out.LoadCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.LoadProductPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.query.CartItemQueryDto;
import com.youthexpedition.azit.modules.store.application.service.mapper.CartResponseMapper;
import com.youthexpedition.azit.modules.store.domain.model.CartItem;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import com.youthexpedition.azit.modules.store.domain.model.ProductSku;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements CartUseCase {
    private final LoadCartPort loadCartPort;
    private final SaveCartPort saveCartPort;
    private final LoadProductPort loadProductPort;
    private final CartResponseMapper cartResponseMapper;

    @Override
    public void addOrUpdateCartItem(AddToCartCommand command) {
        // 장바구니에 이미 동일한 SKU가 있는지 조회
        Optional<CartItem> existingItem = loadCartPort.findByMemberIdAndSkuId(command.memberId(), command.productSkuId());

        // 이미 항목이 있다면 바로 수량 업데이트
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();

            // 재고 확인
            if (cartItem.getSku().getStockQuantity() < cartItem.getQuantity() + command.quantity()) {
                throw new BusinessException(StoreErrorCode.OUT_OF_STOCK);
            }

            saveCartPort.addQuantity(existingItem.get().getId(), command.quantity());
            return;
        }

        // 상품 정보 조회
        Product product = loadProductPort.findByIdForCart(command.productId())
                .orElseThrow(() -> new BusinessException(StoreErrorCode.PRODUCT_NOT_FOUND));

        ProductSku sku = product.getSkus().stream()
                .filter(s -> s.getId().equals(command.productSkuId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(StoreErrorCode.SKU_NOT_FOUND));

        // 요청 수량이 재고를 초과하는지 확인
        if (sku.getStockQuantity() < command.quantity()) {
            throw new BusinessException(StoreErrorCode.OUT_OF_STOCK);
        }

        // 업데이트 또는 저장
        CartItem newItem = CartItem.create(command.memberId(), product, sku, command.quantity());
        saveCartPort.save(newItem);
    }

    @Override
    public void deleteCartItems(Long memberId, CartItemDeleteCommand command) {
        if (command.cartItemIds() == null || command.cartItemIds().isEmpty()) {
            return;
        }

        saveCartPort.deleteAllByMemberIdAndIds(memberId, command.cartItemIds());
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemCountResponse getCartItemCount(Long memberId) {
        long count = loadCartPort.countByMemberId(memberId);

        return cartResponseMapper.toCountResponse(count);
    }

    @Override
    @Transactional(readOnly = true)
    public CartListResponse getCarts(Long memberId) {
        List<CartItemQueryDto> cartItems = loadCartPort.findCartDetailsByMemberId(memberId);

        long totalProductPrice = 0;
        long totalMembershipDiscount = 0;

        // 브랜드별 최대 배송비 저장하는 맵
        Map<Long, Long> brandMaxShippingFeesMap = new HashMap<>();

        for (CartItemQueryDto cartItem : cartItems) {
            long itemBasePrice = cartItem.basePrice() + cartItem.additionalPrice();
            long itemSalePrice = cartItem.salePrice() + cartItem.additionalPrice();

            totalProductPrice += itemBasePrice * cartItem.quantity();
            totalMembershipDiscount += (itemBasePrice - itemSalePrice) * cartItem.quantity();

            // 같은 브랜드의 배송비는 한 번만 반영되도록 맵에 저장
            brandMaxShippingFeesMap.merge(cartItem.brandId(), cartItem.shippingFee(), Long::max);
        }

        // 브랜드별로 집계된 배송비의 총합 계산
        long totalShippingFee = brandMaxShippingFeesMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return cartResponseMapper.toCartListResponse(cartItems, totalProductPrice, totalMembershipDiscount, totalShippingFee);
    }
}
