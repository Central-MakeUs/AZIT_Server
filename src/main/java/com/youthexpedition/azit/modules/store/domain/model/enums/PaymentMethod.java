package com.youthexpedition.azit.modules.store.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    NAVER_PAY("NAVER_PAY", "네이버페이"),
    BANK_TRANSFER("BANK_TRANSFER", "무통장 입금");

    private final String code;
    private final String label;
}
