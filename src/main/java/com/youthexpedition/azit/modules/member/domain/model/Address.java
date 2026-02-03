package com.youthexpedition.azit.modules.member.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Address {
    private final Long id;
    private final Long memberId;
    private String recipientName;  // 수령인
    private String phoneNumber;    // 연락처
    private String zipcode;        // 우편번호
    private String address;        // 기본 주소
    private String detailAddress;  // 상세 주소
    private boolean isDefault;     // 기본 배송지 여부
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long createdBy;
    private Long updatedBy;

    /**
     * 배송지 정보 수정
     */
    public void update(String recipientName, String phoneNumber, String zipcode, String address, String detailAddress) {
        validateInfo(recipientName, phoneNumber, zipcode, address);

        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.zipcode = zipcode;
        this.address = address;
        this.detailAddress = detailAddress;
    }

    /**
     * 기본 배송지 설정/해제
     */
    public void markAsDefault() {
        this.isDefault = true;
    }

    public void markAsNonDefault() {
        this.isDefault = false;
    }

    /**
     * 도메인 수준의 유효성 검사
     * 필수 항목이 누락되지 않았는지 확인합니다.
     */
    private void validateInfo(String recipientName, String phoneNumber, String zipcode, String address) {
        if (recipientName == null || recipientName.isBlank()) throw new IllegalArgumentException("수령인은 필수입니다.");
        if (phoneNumber == null || phoneNumber.isBlank()) throw new IllegalArgumentException("연락처는 필수입니다.");
        if (zipcode == null || zipcode.isBlank()) throw new IllegalArgumentException("우편번호는 필수입니다.");
        if (address == null || address.isBlank()) throw new IllegalArgumentException("주소는 필수입니다.");
    }
}
