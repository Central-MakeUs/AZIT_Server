package com.youthexpedition.azit.modules.store.adapter.out.persistence;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.CartItemEntity;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper.CartMapper;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.repository.CartItemRepository;
import com.youthexpedition.azit.modules.store.application.port.out.LoadCartPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveCartPort;
import com.youthexpedition.azit.modules.store.domain.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartPersistenceAdapter implements LoadCartPort, SaveCartPort {
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;

    @Override
    public void save(CartItem cartItem) {
        CartItemEntity entity = cartMapper.toEntity(cartItem);
        cartItemRepository.save(entity);
    }

    @Override
    public void addQuantity(Long cartItemId, int quantity) {
        cartItemRepository.addQuantity(cartItemId, quantity);
    }

    @Override
    public Optional<CartItem> findByMemberIdAndSkuId(Long memberId, Long productSkuId) {
        return cartItemRepository.findByMemberIdAndSkuId(memberId, productSkuId)
                .map(cartMapper::toDomain);
    }

    @Override
    public void deleteAllByMemberIdAndIds(Long memberId, List<Long> cartItemIds) {
        cartItemRepository.deleteAllByMemberIdAndIds(memberId, cartItemIds);
    }
}
