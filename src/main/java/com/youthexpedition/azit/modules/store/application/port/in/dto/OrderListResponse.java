package com.youthexpedition.azit.modules.store.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youthexpedition.azit.modules.store.domain.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record OrderListResponse(
        @Schema(description = "주문 ID")
        Long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "주문 날짜")
        LocalDateTime orderDate,
        @Schema(description = "주문 번호")
        String orderNumber,
        @Schema(description = "주문 상태")
        OrderStatus status,
        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items
) {

        public static OrderListResponse of(Long id, LocalDateTime orderDate, String orderNumber, OrderStatus status, List<OrderItemResponse> items) {
                return new OrderListResponse(
                        id,
                        orderDate,
                        orderNumber,
                        status,
                        items
                );
        }
}