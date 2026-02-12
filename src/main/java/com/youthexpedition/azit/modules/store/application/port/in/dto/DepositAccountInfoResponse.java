package com.youthexpedition.azit.modules.store.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record DepositAccountInfoResponse(
        @Schema(description = "은행명")
        String bankName,
        @Schema(description = "계좌번호")
        String accountNumber,
        @Schema(description = "예금주")
        String accountHolder,
        @Schema(description = "입금자명")
        String depositorName,
        @Schema(description = "입금 기한")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime paymentDeadline
) {
    public static DepositAccountInfoResponse of(String bankName, String accountNumber, String accountHolder, String depositorName, LocalDateTime paymentDeadline) {
        return new DepositAccountInfoResponse(bankName, accountNumber, accountHolder, depositorName, paymentDeadline);
    }
}
