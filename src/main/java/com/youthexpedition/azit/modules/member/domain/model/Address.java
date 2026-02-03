package com.youthexpedition.azit.modules.member.domain.model;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.member.domain.model.enums.AddressErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;

@Getter
@Builder
@AllArgsConstructor
public class Address {
    private final Long id;
    private final Long memberId;
    private String recipientName;  // 수령인
    private String phoneNumber;    // 연락처
    private String zipcode;        // 우편번호
    private String baseAddress;        // 기본 주소
    private String detailAddress;  // 상세 주소
    private boolean isDefault;     // 기본 배송지 여부
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long createdBy;
    private Long updatedBy;

    public static Address create(Long memberId, String recipientName, String phoneNumber,
                                 String zipcode, String baseAddress, String detailAddress, boolean isDefault) {

        validateInfo(recipientName, phoneNumber, zipcode, baseAddress, detailAddress);

        return Address.builder()
                .memberId(memberId)
                .recipientName(recipientName)
                .phoneNumber(phoneNumber)
                .zipcode(zipcode)
                .baseAddress(baseAddress)
                .detailAddress(detailAddress)
                .isDefault(isDefault)
                .build();
    }

    // 배송지 정보 수정
    public void update(String recipientName, String phoneNumber, String zipcode, String baseAddress, String detailAddress) {
        validateInfo(recipientName, phoneNumber, zipcode, baseAddress, detailAddress);

        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.zipcode = zipcode;
        this.baseAddress = baseAddress;
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

    // 주소 필수 항목 확인
    private static void validateInfo(String recipientName, String phoneNumber, String zipcode, String baseAddress, String detailAddress) {
        if (isNull(recipientName) || recipientName.isEmpty() ||
                isNull(phoneNumber) || phoneNumber.isEmpty() ||
                isNull(zipcode) || zipcode.isEmpty() ||
                isNull(baseAddress) || baseAddress.isEmpty() ||
                isNull(detailAddress) || detailAddress.isEmpty()) {
            throw new BusinessException(AddressErrorCode.INVALID_ADDRESS_INPUT);
        }
    }
}
