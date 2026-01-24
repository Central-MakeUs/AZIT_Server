package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, ProductRepositoryCustom {
}
