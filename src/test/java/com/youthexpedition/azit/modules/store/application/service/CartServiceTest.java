package com.youthexpedition.azit.modules.store.application.service;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.store.application.port.in.command.AddToCartCommand;
import com.youthexpedition.azit.modules.store.application.port.in.command.CartItemDeleteCommand;
import com.youthexpedition.azit.modules.store.application.port.out.LoadCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.LoadProductPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveCartPort;
import com.youthexpedition.azit.modules.store.domain.model.CartItem;
import com.youthexpedition.azit.modules.store.domain.model.Product;
import com.youthexpedition.azit.modules.store.domain.model.ProductSku;
import com.youthexpedition.azit.modules.store.domain.model.enums.StoreErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 단위 테스트")
class CartServiceTest {

    @Mock
    private LoadCartPort loadCartPort;
    @Mock
    private SaveCartPort saveCartPort;
    @Mock
    private LoadProductPort loadProductPort;

    @InjectMocks
    private CartService cartService;

    @Nested
    @DisplayName("장바구니 추가/업데이트")
    class AddOrUpdateCartItem {

        private final Long memberId = 1L;
        private final Long productId = 10L;
        private final Long skuId = 100L;
        private final int quantity = 2;

        private ProductSku sku;
        private Product product;
        private AddToCartCommand command;

        @BeforeEach
        void setUp() {
            command = AddToCartCommand.of(memberId, productId, skuId, quantity);
            sku = ProductSku.builder()
                    .id(skuId)
                    .stockQuantity(10)
                    .build();
            product = Product.builder()
                    .id(productId)
                    .skus(List.of(sku))
                    .build();
        }

        @Test
        @DisplayName("성공 - 새 상품 추가")
        void addCartItem_success_newItem() {
            // given
            when(loadCartPort.findByMemberIdAndSkuId(memberId, skuId)).thenReturn(Optional.empty());
            when(loadProductPort.findByIdForCart(productId)).thenReturn(Optional.of(product));
            doNothing().when(saveCartPort).save(any(CartItem.class));

            // when
            cartService.addOrUpdateCartItem(command);

            // then
            verify(saveCartPort, times(1)).save(any(CartItem.class));
            verify(saveCartPort, never()).addQuantity(anyLong(), anyInt());
        }

        @Test
        @DisplayName("성공 - 기존 상품 수량 추가")
        void addCartItem_success_updateQuantity() {
            // given
            CartItem existingItem = CartItem.builder().id(1L).sku(sku).quantity(3).build();
            when(loadCartPort.findByMemberIdAndSkuId(memberId, skuId)).thenReturn(Optional.of(existingItem));

            // when
            cartService.addOrUpdateCartItem(command);

            // then
            verify(saveCartPort, times(1)).addQuantity(existingItem.getId(), quantity);
            verify(saveCartPort, never()).save(any(CartItem.class));
        }

        @Test
        @DisplayName("실패 - 재고 부족 (새 상품 추가 시)")
        void addCartItem_fail_outOfStock_newItem() {
            // given
            sku = ProductSku.builder().id(skuId).stockQuantity(1).build(); // 재고 1개
            product = Product.builder().id(productId).skus(List.of(sku)).build();
            command = AddToCartCommand.of(memberId, productId, skuId, 2); // 2개 요청

            when(loadCartPort.findByMemberIdAndSkuId(memberId, skuId)).thenReturn(Optional.empty());
            when(loadProductPort.findByIdForCart(productId)).thenReturn(Optional.of(product));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    cartService.addOrUpdateCartItem(command)
            );
            assertEquals(StoreErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 재고 부족 (기존 상품 수량 추가 시)")
        void addCartItem_fail_outOfStock_updateQuantity() {
            // given
            sku = ProductSku.builder().id(skuId).stockQuantity(5).build(); // 재고 5개
            CartItem existingItem = CartItem.builder().id(1L).sku(sku).quantity(4).build(); // 이미 4개 담음
            command = AddToCartCommand.of(memberId, productId, skuId, 2); // 2개 추가 요청 (총 6개)

            when(loadCartPort.findByMemberIdAndSkuId(memberId, skuId)).thenReturn(Optional.of(existingItem));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    cartService.addOrUpdateCartItem(command)
            );
            assertEquals(StoreErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 상품을 찾을 수 없음")
        void addCartItem_fail_productNotFound() {
            // given
            when(loadCartPort.findByMemberIdAndSkuId(memberId, skuId)).thenReturn(Optional.empty());
            when(loadProductPort.findByIdForCart(productId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    cartService.addOrUpdateCartItem(command)
            );
            assertEquals(StoreErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - SKU를 찾을 수 없음")
        void addCartItem_fail_skuNotFound() {
            // given
            product = Product.builder().id(productId).skus(Collections.emptyList()).build(); // SKU가 없는 상품
            when(loadCartPort.findByMemberIdAndSkuId(memberId, skuId)).thenReturn(Optional.empty());
            when(loadProductPort.findByIdForCart(productId)).thenReturn(Optional.of(product));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    cartService.addOrUpdateCartItem(command)
            );
            assertEquals(StoreErrorCode.SKU_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("장바구니 삭제")
    class DeleteCartItems {

        private final Long memberId = 1L;

        @Test
        @DisplayName("성공")
        void deleteCartItems_success() {
            // given
            List<Long> itemIds = List.of(1L, 2L, 3L);
            CartItemDeleteCommand command = new CartItemDeleteCommand(itemIds);
            doNothing().when(saveCartPort).deleteAllByMemberIdAndIds(memberId, itemIds);

            // when
            cartService.deleteCartItems(memberId, command);

            // then
            verify(saveCartPort, times(1)).deleteAllByMemberIdAndIds(memberId, itemIds);
        }

        @Test
        @DisplayName("성공 - 빈 리스트")
        void deleteCartItems_success_emptyList() {
            // given
            CartItemDeleteCommand command = new CartItemDeleteCommand(Collections.emptyList());

            // when
            cartService.deleteCartItems(memberId, command);

            // then
            verify(saveCartPort, never()).deleteAllByMemberIdAndIds(anyLong(), anyList());
        }

        @Test
        @DisplayName("성공 - null 리스트")
        void deleteCartItems_success_nullList() {
            // given
            CartItemDeleteCommand command = new CartItemDeleteCommand(null);

            // when
            cartService.deleteCartItems(memberId, command);

            // then
            verify(saveCartPort, never()).deleteAllByMemberIdAndIds(anyLong(), anyList());
        }
    }
}
