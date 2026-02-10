package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, OrderRepositoryCustom {
    boolean existsByOrderNumber(String orderNumber);
}
