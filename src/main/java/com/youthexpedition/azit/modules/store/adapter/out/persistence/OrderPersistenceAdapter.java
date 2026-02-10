package com.youthexpedition.azit.modules.store.adapter.out.persistence;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.OrderEntity;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper.OrderMapper;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.repository.OrderRepository;
import com.youthexpedition.azit.modules.store.application.port.out.LoadOrderPort;
import com.youthexpedition.azit.modules.store.application.port.out.SaveOrderPort;
import com.youthexpedition.azit.modules.store.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity savedEntity = orderRepository.save(entity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return orderRepository.existsByOrderNumber(orderNumber);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(orderMapper::toDomain);
    }

}
