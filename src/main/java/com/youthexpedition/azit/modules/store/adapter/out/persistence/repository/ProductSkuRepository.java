package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.ProductSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductSkuRepository extends JpaRepository<ProductSkuEntity, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductSkuEntity s SET s.stockQuantity = s.stockQuantity - :quantity WHERE s.id = :skuId AND s.stockQuantity >= :quantity")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ProductSkuEntity s SET s.stockQuantity = s.stockQuantity + :quantity WHERE s.id = :skuId")
    void increaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
