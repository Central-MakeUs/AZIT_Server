package com.youthexpedition.azit.modules.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderAddress {
    String recipientName;
    String phoneNumber;
    String baseAddress;
    String detailAddress;
}
