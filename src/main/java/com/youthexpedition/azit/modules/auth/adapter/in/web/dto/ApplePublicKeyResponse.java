package com.youthexpedition.azit.modules.auth.adapter.in.web.dto;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;

import java.util.List;

public record ApplePublicKeyResponse(
        List<ApplePublicKey> keys
) {
    public record ApplePublicKey(
            String kty, // 키 타입 (예: RSA)
            String kid, // 키 식별자
            String use, // 사용 용도 (예: sig - 서명용)
            String alg, // 알고리즘 (예: RS256)
            String n,   // RSA 모듈러스
            String e    // RSA 지수
    ) {}

    // 특정 kid와 alg에 매칭되는 키를 찾는 메서드
    public ApplePublicKey getMatchedKey(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AuthErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND));
    }
}
