package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.common.response.code.CommonErrorCode;
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
import com.youthexpedition.azit.modules.store.domain.model.OrderPricePolicy;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import com.youthexpedition.azit.modules.store.domain.model.ProductSku;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    @Transactional
    public void updateCartItemQuantity(Long memberId, Long cartItemId, int quantity) {
        CartItem cartItem = loadCartPort.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(StoreErrorCode.CART_ITEM_NOT_FOUND));

        // 본인의 장바구니 아이템인지 확인
        if (!cartItem.getMemberId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN_ERROR);
        }

        // 재고 확인
        if (cartItem.getSku().getStockQuantity() < quantity) {
            throw new BusinessException(StoreErrorCode.OUT_OF_STOCK);
        }

        // 수량 업데이트
        cartItem.updateQuantity(quantity);
        saveCartPort.save(cartItem);
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

        long totalProductPrice = OrderPricePolicy.calculateTotalProductPrice(cartItems);
        long totalMembershipDiscount = OrderPricePolicy.calculateTotalMembershipDiscount(cartItems);
        long totalShippingFee = OrderPricePolicy.calculateTotalShippingFee(cartItems);

        return cartResponseMapper.toCartListResponse(cartItems, totalProductPrice, totalMembershipDiscount, totalShippingFee);
    }
}
