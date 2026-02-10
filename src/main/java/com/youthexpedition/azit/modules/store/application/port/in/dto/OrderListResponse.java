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
        List<OrderItemSummaryResponse> items
) {
        public record OrderItemSummaryResponse(
                @Schema(description = "구매 당시 브랜드명")
                String brandName,
                @Schema(description = "구매 당시 상품명")
                String productName,
                @Schema(description = "구매 당시 옵션 정보")
                String optionDescription,
                @Schema(description = "구매 당시 상품 대표 이미지 url")
                String productImageUrl,
                @Schema(description = "판매가")
                long salePrice,
                @Schema(description = "구매 수량")
                int quantity
        ) {
                public static OrderItemSummaryResponse of(String brandName, String productName, String optionDescription,
                                                   String productImageUrl, long totalSalePrice, int quantity) {
                        return new OrderItemSummaryResponse(
                                brandName,
                                productName,
                                optionDescription,
                                productImageUrl,
                                totalSalePrice,
                                quantity);
                }
        }

        public static OrderListResponse of(Long id, LocalDateTime orderDate, String orderNumber, OrderStatus status, List<OrderItemSummaryResponse> items) {
                return new OrderListResponse(
                        id,
                        orderDate,
                        orderNumber,
                        status,
                        items
                );
        }
}