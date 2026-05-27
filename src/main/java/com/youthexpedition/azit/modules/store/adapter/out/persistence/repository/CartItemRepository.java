package com.youthexpedition.azit.modules.store.adapter.out.persistence.repository;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long>, CartItemRepositoryCustom {
    @Query("SELECT ci FROM CartItemEntity ci " +
            "JOIN FETCH ci.product p " +
            "JOIN FETCH ci.sku s " +
            "WHERE ci.memberId = :memberId AND ci.sku.id = :skuId")
    Optional<CartItemEntity> findByMemberIdAndSkuId(Long memberId, Long skuId);

    @Modifying
    @Query("UPDATE CartItemEntity ci SET ci.quantity = ci.quantity + :quantity WHERE ci.id = :id")
    void addQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CartItemEntity ci WHERE ci.memberId = :memberId AND ci.id IN :ids")
    void deleteAllByMemberIdAndIds(@Param("memberId") Long memberId, @Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CartItemEntity ci WHERE ci.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    long countByMemberId(Long memberId);
}
