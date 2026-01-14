package com.youthexpedition.azit.infrastructure.auth.util;

import com.youthexpedition.azit.infrastructure.exception.BusinessException;
import com.youthexpedition.azit.modules.auth.adapter.in.web.dto.ApplePublicKeyResponse;
import com.youthexpedition.azit.modules.auth.domain.model.enums.AuthErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AppleJwtUtils {
    @Value("${oauth.apple.team-id}")
    private String teamId;

    @Value("${oauth.apple.client-id}")
    private String clientId;

    @Value("${oauth.apple.key-id}")
    private String keyId;

    @Value("${oauth.apple.key-path}")
    private String keyPath;

    /**
     * Apple Client Secret (JWT) 생성
     */
    public String createClientSecret() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusMinutes(5);

        return Jwts.builder()
                .header()
                    .keyId(keyId) // Apple Key ID
                    .add("alg", "ES256")
                .and()
                .issuer(teamId) // Apple Team ID
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .audience()
                    .add("https://appleid.apple.com")
                .and()
                .subject(clientId) // Apple Service ID
                .signWith(getPrivateKey()) // .p8 키로 서명
                .compact();
    }

    /**
     * .p8 파일로부터 PrivateKey 객체 획득
     */
    private PrivateKey getPrivateKey() {
        try {
            // 파일 읽기
            String content = new String(Files.readAllBytes(Paths.get(keyPath)));

            // PEM 키에서 헤더/푸터 및 공백 제거
            String privateKeyPEM = content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            // Base64 디코딩
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            // PKCS#8 알고리즘을 사용해 PrivateKey 생성
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new BusinessException(AuthErrorCode.APPLE_CLIENT_SECRET_CREATION_FAILED);
        }
    }

    /**
     * Apple ID Token 서명 검증 및 Claims 추출
     */
    public Claims verifyIdToken(String idToken, ApplePublicKeyResponse.ApplePublicKey publicKey) {
        try {
            // n, e 값을 이용해 공개키 생성
            byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
            byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    new BigInteger(1, nBytes),
                    new BigInteger(1, eBytes)
            );
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey rsaPublicKey = keyFactory.generatePublic(publicKeySpec);

            // 토큰 검증 및 파싱
            return Jwts.parser()
                    .verifyWith(rsaPublicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

        } catch (Exception e) {
            throw new BusinessException(AuthErrorCode.INVALID_APPLE_ID_TOKEN);
        }
    }

    /**
     * ID Token 헤더에서 kid, alg 추출을 위해 토큰 파싱 (서명 검증 전)
     */
    public JwsHeader getHeader(String idToken) {
        try {
            return Jwts.parser()
                    .build()
                    .parseSignedClaims(idToken)
                    .getHeader();
        } catch (Exception e) {
            throw new BusinessException(AuthErrorCode.INVALID_APPLE_ID_TOKEN);
        }
    }
}
