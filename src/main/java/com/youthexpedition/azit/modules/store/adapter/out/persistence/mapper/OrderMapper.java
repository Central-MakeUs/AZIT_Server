package com.youthexpedition.azit.modules.store.adapter.out.persistence.mapper;

import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.OrderEntity;
import com.youthexpedition.azit.modules.store.adapter.out.persistence.entity.OrderItemEntity;
import com.youthexpedition.azit.modules.store.domain.model.Order;
import com.youthexpedition.azit.modules.store.domain.model.OrderAddress;
import com.youthexpedition.azit.modules.store.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    public Order toDomain(OrderEntity entity) {
        if (entity == null) return null;

        return Order.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .orderNumber(entity.getOrderNumber())
                .address(toOrderAddress(entity))
                .shippingInstruction(entity.getShippingInstruction())
                .totalProductPrice(entity.getTotalProductPrice())
                .totalShippingFee(entity.getTotalShippingFee())
                .membershipDiscount(entity.getMembershipDiscount())
                .usedPoints(entity.getUsedPoints())
                .totalPaymentPrice(entity.getTotalPaymentPrice())
                .paymentMethod(entity.getPaymentMethod())
                .courier(entity.getCourier())
                .trackingNumber(entity.getTrackingNumber())
                .status(entity.getStatus())
                .orderItems(toOrderItems(entity.getOrderItems()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private OrderItem toItemDomain(OrderItemEntity entity) {
        if (entity == null) return null;

        return OrderItem.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .skuId(entity.getSkuId())
                .productName(entity.getProductName())
                .optionDescription(entity.getOptionDescription())
                .basePrice(entity.getBasePrice())
                .salePrice(entity.getSalePrice())
                .quantity(entity.getQuantity())
                .build();
    }

    public OrderEntity toEntity(Order domain) {
        if (domain == null) return null;

        OrderEntity orderEntity = OrderEntity.builder()
                .memberId(domain.getMemberId())
                .orderNumber(domain.getOrderNumber())
                .recipientName(domain.getAddress().getRecipientName())
                .phoneNumber(domain.getAddress().getPhoneNumber())
                .baseAddress(domain.getAddress().getBaseAddress())
                .detailAddress(domain.getAddress().getDetailAddress())
                .shippingInstruction(domain.getShippingInstruction())
                .totalProductPrice(domain.getTotalProductPrice())
                .totalShippingFee(domain.getTotalShippingFee())
                .membershipDiscount(domain.getMembershipDiscount())
                .usedPoints(domain.getUsedPoints())
                .totalPaymentPrice(domain.getTotalPaymentPrice())
                .paymentMethod(domain.getPaymentMethod())
                .courier(domain.getCourier())
                .trackingNumber(domain.getTrackingNumber())
                .status(domain.getStatus())
                .build();

        domain.getOrderItems().stream()
                .map(this::toItemEntity)
                .forEach(orderEntity::addOrderItem);

        return orderEntity;
    }

    private OrderItemEntity toItemEntity(OrderItem domain) {
        if (domain == null) return null;

        return OrderItemEntity.builder()
                .productId(domain.getProductId())
                .skuId(domain.getSkuId())
                .productName(domain.getProductName())
                .optionDescription(domain.getOptionDescription())
                .basePrice(domain.getBasePrice())
                .salePrice(domain.getSalePrice())
                .quantity(domain.getQuantity())
                .build();
    }

    private OrderAddress toOrderAddress(OrderEntity entity) {
        return OrderAddress.builder()
                .recipientName(entity.getRecipientName())
                .phoneNumber(entity.getPhoneNumber())
                .baseAddress(entity.getBaseAddress())
                .detailAddress(entity.getDetailAddress())
                .build();
    }

    private List<OrderItem> toOrderItems(List<OrderItemEntity> itemEntities) {
        return itemEntities.stream()
                .map(this::toItemDomain)
                .toList();
    }
}