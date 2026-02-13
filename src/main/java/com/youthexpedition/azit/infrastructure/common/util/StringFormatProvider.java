package com.youthexpedition.azit.infrastructure.common.util;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StringFormatProvider {

    private static final String OPTION_SEPARATOR = " · ";
    private static final String ORDER_NUMBER_PREFIX = "#";

    /**
     * 옵션 + · + 옵션 형식으로 조합
     */
    public String formatOptionValues(List<String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return "";
        }
        return String.join(OPTION_SEPARATOR, optionValues);
    }

    /**
     * 주문 번호 앞에 접두어(#) 붙임
     */
    public String buildFullOrderNumber(String orderNumber) {
        if (orderNumber == null) return null;
        return ORDER_NUMBER_PREFIX + orderNumber;
    }
}
