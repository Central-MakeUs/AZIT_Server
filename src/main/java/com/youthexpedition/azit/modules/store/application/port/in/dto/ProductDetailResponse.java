package com.youthexpedition.azit.modules.store.application.port.in.dto;

import com.youthexpedition.azit.modules.store.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ProductDetailResponse(
        @Schema(description = "상품 ID")
        Long id,
        @Schema(description = "브랜드명")
        String brandName,
        @Schema(description = "상품명")
        String productName,
        @Schema(description = "정가")
        Long basePrice,
        @Schema(description = "할인율(%)")
        Integer discountRate,
        @Schema(description = "최종 판매가")
        Long salePrice,
        @Schema(description = "배송비")
        Long shippingFee,
        @Schema(description = "예상 발송 시작일")
        LocalDate expectedShippingDate,
        @Schema(description = "환불 정책")
        String refundPolicy,
        @Schema(description = "상품 상세 설명")
        String description,
        @Schema(description = "상단 슬라이드 이미지 URL 리스트")
        List<String> slideImageUrls,
        @Schema(description = "하단 상세 설명 이미지 URL 리스트")
        List<String> detailImageUrls,
        @Schema(description = "옵션 그룹 목록")
        List<OptionGroupResponse> optionGroups,
        @Schema(description = "재고 및 옵션 조합 정보 (SKU)")
        List<SkuResponse> skus
) {
        public record OptionGroupResponse(
                @Schema(description = "옵션 그룹 ID")
                Long id,
                @Schema(description = "옵션 그룹 명칭")
                String name,
                @Schema(description = "그룹에 속한 옵션값 목록")
                List<OptionValueResponse> values
        ) {}
        public record OptionValueResponse(
                @Schema(description = "옵션값 ID")
                Long id,
                @Schema(description = "옵션값 명칭")
                String value
        ) {}
        public record SkuResponse(
                @Schema(description = "SKU ID")
                Long id,
                @Schema(description = "옵션 별 추가 금액")
                Long additionalPrice,
                @Schema(description = "재고 수량")
                Integer stockQuantity,
                @Schema(description = "매칭되는 옵션값 ID 리스트 (정렬된 순서)")
                List<Long> optionValueIds
        ) {}

        public static ProductDetailResponse of(Product product, List<String> slideImageUrls, List<String> detailImageUrls,
                                               List<OptionGroupResponse> optionGroups, List<SkuResponse> skus
        ) {
                return new ProductDetailResponse(
                        product.getId(),
                        product.getBrand().getName(),
                        product.getName(),
                        product.getBasePrice(),
                        product.getDiscountRate(),
                        product.getSalePrice(),
                        product.getShippingFee(),
                        product.calculateExpectedShippingDate(),
                        product.getRefundPolicy(),
                        product.getDescription(),
                        slideImageUrls,
                        detailImageUrls,
                        optionGroups,
                        skus
                );
        }
}